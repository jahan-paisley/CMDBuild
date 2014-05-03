package models
{
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.net.URLLoader;
    import flash.net.URLRequest;
    import flash.net.URLVariables;
    import flash.net.URLRequestMethod;
    import flash.xml.*;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;

    import flare.data.DataSet;
    import flare.data.DataTable;
    import flare.data.converters.GraphMLConverter;
    import flare.vis.data.Data;
    import flare.vis.data.NodeSprite;
    import flare.vis.data.EdgeSprite;

    import mx.utils.ObjectUtil;

    import models.DataStorage;
    import models.events.CmdbDataEvent;

    import utils.GraphUtil;

    public class CmdbData extends EventDispatcher
    {
        private var _baseUrl:String = "services/json/legacy/graph";
        private var _xmlDoc:XMLDocument = new XMLDocument();

        private var _currentClass:String;
        private var _currentObjectId:int;
        private var _currentLevel:int;
        private var _lastLoadedLevel:int;

        private var _requestQueue:Array = new Array();
        private var _requestRunning:Boolean = false;

        private var _card:ArrayCollection = new ArrayCollection();

        private var _dataRelations:ArrayCollection = new ArrayCollection();

        private var _data:DataStorage = new DataStorage();

        public function get relations():ArrayCollection {
            _dataRelations.filterFunction = function(value:Object):Boolean {
                return value.level <= _currentLevel;
            };
            _dataRelations.refresh();
            return _dataRelations;
        }

        public function get card():ArrayCollection { return _card; }

        public function get data():DataStorage { return _data; }

        public function get level():int { return _currentLevel; }
        public function set level(newLevel:int):void {
            _currentLevel = newLevel;
            var lll:int = _lastLoadedLevel;
            if(newLevel > _lastLoadedLevel){
                var gap:int = newLevel - _lastLoadedLevel;
                for (var i:int = 0; i < gap; i++) {
                    var url:String = _baseUrl;
                    sendToServer(url, prepareRequest, parseResponse, {level: lll + i});
                }
            }
            else {
                dispatchEvent(new CmdbDataEvent(CmdbDataEvent.NEW_DATA));
            }
        }

        public function CmdbData() {
            _currentLevel = GraphUtil.parameters.baseLevel;
            var sort:Sort = new Sort();
            sort.fields = [new SortField("level"), new SortField("description")];
            _dataRelations.sort = sort;
        }

        private function addNodeToXML(xml:XML, container:String, collection:Array):void {
            var xmlNode:XMLNode = _xmlDoc.createElement(container);
            var item:Object;
            var itemNode:XMLNode;
            trace("adding elements to XML: " + collection.length)
            for(var j:int = 0; j < collection.length; j++) {
            trace("adding element: " + ObjectUtil.toString(collection[j]))
                itemNode = _xmlDoc.createElement("item");
                itemNode.attributes = collection[j];
                xmlNode.appendChild(itemNode);
            }
            xml.appendChild(xmlNode);
        }

        public function centerOn(node:Object):void {
            // refresh data
            dispatchEvent(new CmdbDataEvent(CmdbDataEvent.CLEAR));
            _data.clear();
            _dataRelations.source = new Array();
            node.level = 0;
            node.type = "node";
            node.id = GraphUtil.createIdFromData(node);
            _lastLoadedLevel = 0;
            _data.addItem(node);
            // iterate over levels
            for (var i:int = 0; i < _currentLevel; i++) {
                var url:String = _baseUrl;
                sendToServer(url, prepareRequest, parseResponse, {level: i} );
            }
        }

        private function prepareRequest(parameters:Object):URLVariables {
            // Prepare XML for server request
            var xml:XML = new XML(<data></data>);
            // nodes
            addNodeToXML(xml, "nodes", _data.getFilteredData({type: "node", level: parameters.level}));
            // nodes to exclude (already in previous level)
            if(parameters.level > 0)
                addNodeToXML(xml, "excludes", _data.getFilteredData({type: "node", level: parameters.level-1}));
            // filters
            addAllFiltersNodeToXML(xml);
            var variables:URLVariables = new URLVariables();
            variables.method = "graphML";
            variables.data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + xml.toXMLString();
            trace("prepared request for level: " + parameters.level);
            trace(variables.data);
            return variables;
        }

        private function parseResponse(data:XML, parameters:Object):void {
            var item:XML;
            // check for errors
            var errorMessages:XMLList = data.errors.item;
            if(data.errors.length() > 0) {
                var message:String = ""
                for each(item in errorMessages) {
                    message += item.@message
                }
                dispatchEvent(new CmdbDataEvent(CmdbDataEvent.ERROR, {error: message}));
                return;
            }
            // Parse response data
            // Relations
            var relations:XMLList = data.relations.item;
            var attribute:XML;
            var attributeName:String;
            for each(item in relations) {
                var attNamesList:XMLList = item.@*;
                var relItem:Object = {level: parameters.level + 1};
                for (var i:int = 0; i < attNamesList.length(); i++) {
                    attributeName=attNamesList[i].name();
                    switch (attributeName) {
                        case "elements" :
                            relItem[attributeName] = parseInt(item.@[attributeName]);
                            break;
                        case "clusterize" :
                            relItem[attributeName] = (item.@[attributeName] == "true");
                            break;
                        default:
                            relItem[attributeName] = item.@[attributeName];
                    }
                }
                _dataRelations.addItem(relItem);
            }
            // Graph
            var gmlc:GraphMLConverter = new GraphMLConverter();
            var ds:DataSet = gmlc.parse(XML(data.graphml));
            var d:Data = Data.fromDataSet(ds);
            var objects:Array = [];
            trace("parsing this data ... \n" + data.toString());
            d.nodes.visit(function(ns:NodeSprite):void {
                var node:Object = ns.data;
                var checkObj:Object = {id: node.id};
                var pos:int = _data.checkExistence(checkObj);
                trace("pos ... " + pos + "\n");
                if(pos == -1) {
                    node.level = parameters.level + 1;
                    _data.addItem(node);
                }
                else {
                    // refresh data
                    _data.merge(pos, node);
                }
            });
            d.edges.visit(function(es:EdgeSprite):void {
                var edge:Object = {source: es.data.source, target: es.data.target, type: "edge"};
                var pos:int = _data.checkExistence(edge);
                if(pos == -1){
                    edge.domaindescription = es.data.domaindescription;
                    _data.addItem(edge);
                }
                else {
                    edge = _data.getItemAt(pos);
                    edge.domaindescription += " - " + es.data.domaindescription;
                }
            });

            if( (parameters.level + 1) > _lastLoadedLevel) _lastLoadedLevel = parameters.level + 1;

            dispatchEvent(new CmdbDataEvent(CmdbDataEvent.NEW_DATA));
        }

        public function cardFor(node:Object):void {
            var url:String = _baseUrl;
            sendToServer(url,
                function(parameters:Object):URLVariables {
                    var xml:XML = new XML(<data></data>);
                    // nodes
                    addNodeToXML(xml, "nodes", [node]);
                    var variables:URLVariables = new URLVariables();
                    variables.method = "card";
                    variables.data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + xml.toXMLString();
                    trace(variables.data);
                    return variables;
                },
                function(data:XML, parameters:Object):void {
                    // check for errors
                    var item:XML;
                    var errorMessages:XMLList = data.errors.item;
                    if(data.errors.length() > 0) {
                        var message:String = ""
                        for each(item in errorMessages) {
                            message += item.@message
                        }
                        dispatchEvent(new CmdbDataEvent(CmdbDataEvent.ERROR, {error: message}));
                        return;
                    }
                    _card.source = new Array();
                    var card:XMLList = data.card.item;
                    trace("parsing this data ... \n" + data.toXMLString());
                    for each(item in card) {
                        var cardItem:Object = { name: item.@name, value: item.@value };
                        _card.addItem(cardItem);
                    }
                    dispatchEvent(new CmdbDataEvent(CmdbDataEvent.CARD, node));
                }
            );
        }

        public function filterFor(node:Object):void {
            var url:String = _baseUrl;
            var relation:Object = {};
            for(var i:int = 0; i < _dataRelations.length; i++) {
                var r:Object = _dataRelations.getItemAt(i);
                if(GraphUtil.createClusterIdFromRelation(r) == node.id){
                    relation = r;
                    break;
                }
            }
            sendToServer(url,
                function(parameters:Object):URLVariables {
                    var xml:XML = new XML(<data></data>);
                    // nodes
                    addNodeToXML(xml, "relations", [relation]);
                    var variables:URLVariables = new URLVariables();
                    variables.method = "filter";
                    variables.data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + xml.toXMLString();
                    trace(variables.data);
                    return variables;
                    return null;
                },
                function(data:XML, parameters:Object):void {
                    var item:XML;
                    // check for errors
                    var errorMessages:XMLList = data.errors.item;
                    if(data.errors.length() > 0) {
                        var message:String = ""
                        for each(item in errorMessages) {
                            message += item.@message
                        }
                        dispatchEvent(new CmdbDataEvent(CmdbDataEvent.ERROR, {error: message}));
                        return;
                    }
                    _card.source = new Array();
                    var card:XMLList = data.card.item;
                    trace("parsing this data ... \n" + data.toXMLString());
                    for each(item in card) {
                        var realName:String = item.@realName;
                        var name:String = item.@name;
                        var value:String = relation.filter ? relation.filter[realName] : "";
                        var cardItem:Object = { realName: realName, name: name, value: value };
                        _card.addItem(cardItem);
                    }
                    dispatchEvent(new CmdbDataEvent(CmdbDataEvent.FILTER, node));
                }
            );
        }

        private function addAllFiltersNodeToXML(xml:XML):void {
            var xmlNode:XMLNode = _xmlDoc.createElement("filters");
            for(var i:int = 0; i < _dataRelations.length; i++) {
                var r:Object = _dataRelations.getItemAt(i);
                addFilterNodeToXML(xmlNode, r.filter, r);
            }
            xml.appendChild(xmlNode);
        }

        private function addFilterNodeToXML(xmlNode:XMLNode, filter:Object, relation:Object = null):void {
            var item:Object;
            var itemNode:XMLNode;
            for (var p:String in filter) {
                if( filter[p] ) {
                    itemNode = _xmlDoc.createElement("item");
                    var attrs:Object = {name: p, value: filter[p]};
                    if(relation){
                        attrs.parentClassId = relation.parentClassId;
                        attrs.parentObjId = relation.parentObjId;
                        attrs.domainId = relation.domainId;
                    }
                    itemNode.attributes = attrs;
                    xmlNode.appendChild(itemNode);
                }
            }
        }

        public function setFilter(relation:Object):void {
            var filter:Object = {};
            var i:int;
            for(i = 0; i < _card.length; i++) {
                var item:Object = _card.getItemAt(i);
                if(item.value != "") filter[item.realName] = item.value;
            }
            relation.filter = filter;

            var url:String = _baseUrl;
            sendToServer(url,
                function(parameters:Object):URLVariables {
                    var xml:XML = new XML(<data></data>);
                    var xmlNode:XMLNode = _xmlDoc.createElement("relation");
                    xmlNode.attributes = {
                        domainId: relation.domainId,
                        parentClassId: relation.parentClassId,
                        parentObjId: relation.parentObjId };
                    xml.appendChild(xmlNode);
                    var filterNode:XMLNode = _xmlDoc.createElement("filters");
                    addFilterNodeToXML(filterNode, filter);
                    xml.appendChild(filterNode);
                    var variables:URLVariables = new URLVariables();
                    variables.method = "calculate";
                    variables.data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + xml.toXMLString();
                    trace(variables.data);
                    return variables;
                },
                function(data:XML, parameters:Object):void {
                    var item:XML;
                    // check for errors
                    var errorMessages:XMLList = data.errors.item;
                    if(data.errors.length() > 0) {
                        var message:String = ""
                        for each(item in errorMessages) {
                            message += item.@message
                        }
                        relation.filter = {};
                        dispatchEvent(new CmdbDataEvent(CmdbDataEvent.ERROR, {error: message}));
                        return;
                    }
                    trace("parsing this data ... \n" + data.toXMLString());
                    var relations:XMLList = data.relations.item;
                    for each(item in relations) {
                        relation.elements = parseInt(item.@["elements"]);
                    }
                    var cluster:Object = {id: GraphUtil.createClusterIdFromRelation(relation)};
                    var pos:int = _data.checkExistence(cluster);
                    trace("node ... " + pos);
                    if(pos != -1) {
                        cluster = _data.getItemAt(pos);
                        cluster.elements = relation.elements;
                    }
                    trace("relations ... \n" + _dataRelations);
                    dispatchEvent(new CmdbDataEvent(CmdbDataEvent.RELATION_UPDATE, {relation: relation, cluster: cluster}));
                }
            );
        }

        public function manageCluster(clusterize:Boolean, relation:Object):void {
            if(clusterize) {
                var p:Object = _data.clusteringRequestFor(relation);
                p.clusterize = true;
                dispatchEvent(new CmdbDataEvent(CmdbDataEvent.CLUSTER, p));
            }
            else {
                _data.removeNodeById(GraphUtil.createClusterIdFromRelation(relation), true);
                var url:String = _baseUrl;
                sendToServer(url,
                    function(parameters:Object):URLVariables {
                        var xml:XML = new XML(<data></data>);
                        addNodeToXML(xml, "relations", [relation]);
                        var filterNode:XMLNode = _xmlDoc.createElement("filters");
                        addFilterNodeToXML(filterNode, relation.filter);
                        xml.appendChild(filterNode);
                        var variables:URLVariables = new URLVariables();
                        variables.method = "declusterize";
                        variables.data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + xml.toXMLString();
                        trace(variables.data);
                        return variables;
                    },
                    function(data:XML, parameters:Object):void {
                        var item:XML;
                        // check for errors
                        var errorMessages:XMLList = data.errors.item;
                        if(data.errors.length() > 0) {
                            var message:String = "";
                            for each(item in errorMessages) {
                                message += item.@message
                            }
                            dispatchEvent(new CmdbDataEvent(CmdbDataEvent.ERROR, {error: message}));
                            return;
                        }
                        var gmlc:GraphMLConverter = new GraphMLConverter();
                        var ds:DataSet = gmlc.parse(XML(data.graphml));
                        var d:Data = Data.fromDataSet(ds);
                        var objects:Array = [];
                        trace("parsing this data ... \n" + data.toString());
                        d.nodes.visit(function(ns:NodeSprite):void {
                            var node:Object = ns.data;
                            var checkObj:Object = {id: node.id};
                            var pos:int = _data.checkExistence(checkObj);
                            if(pos == -1) {
                                node.level = relation.level;
                                _data.addItem(node);
                            }
                            else {
                                // refresh data
                                _data.merge(pos, node);
                            }
                        });
                        d.edges.visit(function(es:EdgeSprite):void {
                            var edge:Object = {source: es.data.source, target: es.data.target, type: "edge"};
                            var pos:int = _data.checkExistence(edge);
                            if(pos == -1){
                                _data.addItem(edge);
                            }
                        });
                        var p:Object = {clusterize: false, cluster: GraphUtil.createClusterIdFromRelation(relation)};
                        dispatchEvent(new CmdbDataEvent(CmdbDataEvent.CLUSTER, p));
                        //if(relation.level == _lastLoadedLevel) TODO iterate request for deeper levels
                    }
                );
            }
        }

        private function sendToServer(url:String, prepare:Function, completed:Function, optional:Object = null):void {
            _requestQueue.push({url: url, prepare: prepare, completed: completed, optional: optional});
            if( ! _requestRunning ) performRequest();
        }

        private function performRequest():void {
            _requestRunning = true;
            var requestObj:Object = _requestQueue.shift();
            var loader:URLLoader = new URLLoader();
            loader.addEventListener(Event.COMPLETE, function(event:Event):void {
                var loader:URLLoader = URLLoader(event.target);
                if(requestObj.completed) requestObj.completed(XML(loader.data), requestObj.optional);
                if(_requestQueue.length > 0) performRequest();
                _requestRunning = false;
            });
            var request:URLRequest = new URLRequest(requestObj.url);
            request.method = URLRequestMethod.POST;
            if(requestObj.prepare) request.data = requestObj.prepare(requestObj.optional);
            loader.load(request);
        }

    }
}

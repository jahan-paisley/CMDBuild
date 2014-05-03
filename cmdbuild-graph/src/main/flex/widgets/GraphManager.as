package widgets
{
    /* FLASH */
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.net.URLLoader;
    import flash.net.URLRequest;
    import flash.text.StyleSheet;
    import flash.text.TextFieldAutoSize
    import flash.utils.Dictionary;
    import flash.utils.setTimeout;
    import flash.text.TextFormat;
    import flash.display.DisplayObject;

    /* FLEX */
    import mx.controls.*;
    import mx.events.*;
    import mx.events.ResizeEvent;
    import mx.controls.Alert;

    /* FLARE */
    import flare.display.*;
    import flare.data.*;
    import flare.flex.*;
    import flare.vis.*;
    import flare.vis.data.*;
    import flare.data.converters.*;
    import flare.animate.*;
    import flare.vis.operator.*;
    import flare.vis.operator.layout.*;
    import flare.vis.controls.*;
    import flare.analytics.graph.*;
    import flare.vis.events.TooltipEvent;
    import flare.vis.operator.label.Labeler;
    import flare.vis.operator.filter.VisibilityFilter;

    /* LOCALS */
    import extensions.HtmlLabeler;
    import widgets.events.NodeEvent;
    import models.CmdbData;

    import mx.utils.ObjectUtil;

    public class GraphManager extends FlareVis
    {
        private var duration:Number = 0.7;

        public var _colorLevels:Array = [
            ["#0000ff", 0xCCFFBF60],
            ["#0000ff", 0xCCffff99],
            ["#0000ff", 0xCCffffdd],
            ["#0000ff", 0xCCffffff],
            ["#0000ff", 0xCCffffff]
        ];

        public var borderColor:Number = 0xff0000ff;
        public var borderColorSelected:Number = 0xffff0000;

        private var currentSelection:NodeSprite = null;

        private var ttc:TooltipControl = null;
        private var _vf:VisibilityFilter = null;
        private var _root:NodeSprite = null;

        private var offset:Number = 3;
        private var boxWidth:Number = 80;
        private var boxHeight:Number = 50;

        /* This property holds the objects already added to the graph,
           it's based on an identification method shared by client
           and server that take into account nodes and clusters  */
        private var nodes:Object = {};
        private var edges:Object = {};

        private var _os:OperatorSwitch;

        public function GraphManager() {
            var rtl:RadialTreeLayout = new RadialTreeLayout();
            var cl:CircleLayout = new CircleLayout();
            _os = new OperatorSwitch( rtl, cl );
            visualization.operators.add(_os);
            _os.index = 0;

            var textFormat:TextFormat = new TextFormat();
            textFormat.color = 0x0e010e;
            textFormat.font = "Arial";
            var ll:HtmlLabeler = new HtmlLabeler(nodeLabel, Data.NODES, textFormat);
            ll.width = boxWidth;
            ll.height = boxHeight;
            var css:StyleSheet = ll.css;
            var p:Object = new Object();
            p.fontFamily = "Arial";
            p.color = _colorLevels[0][0];
            p.fontSize = 9;
            css.setStyle("p", p);
            ll.textMode = 0;
            visualization.operators.add( ll );

            visualization.controls.add(new PanZoomControl(this));
            visualization.controls.add(new DragControl());

            ttc = new TooltipControl(function(n:DisplayObject):Boolean {return n is EdgeSprite}, null, tooltipShowEvent, null, null, 200);
            TextSprite(ttc.tooltip).text = "";
            visualization.controls.add(ttc);

            // Add empty dataset
            visualization.data = new Data();
        }

        public function nodeLabel( d:DataSprite ):String {
            if(d.data.type == "cluster")
                return "<p align='center'><b>" + d.data.classDesc + "</b><br>" + d.data.elements + " elements</p>";
            else
                return "<p align='center'><b>" + d.data.classDesc + "</b><br>" + d.data.objDesc + "</p>";
        }

        public function tooltipShowEvent( evt:TooltipEvent=null ):void {
            if(evt.edge) TextSprite(ttc.tooltip).text = evt.edge.data.domaindescription;
        }

        private function setGeometry():void {
            var w:Number = parent.width;
            var h:Number = parent.height;
            visualization.bounds = new Rectangle(0, 50, w, h-100);
            scrollRect = new Rectangle(0, 0, w, h);
        }

        public function loadData( cmdbData:CmdbData, n:NodeSprite = null ):void {
            setGeometry();
            if(buildNodes(cmdbData, n)) operate();
        }

        public function clearData():void {
            try {
                visualization.data = new Data();
                nodes = {};
                edges = {};
                visualization.update();
            }
            catch(e:Error){
            }
        }

        public function containerResized(evt:ResizeEvent):void {
            try {
                setGeometry();
                setTimeout(function():void {
                    visualization.update();
                }, 100);
            }
            catch(e:Error){
            }
        }

        public function manageCluster(cmdbData:CmdbData, parameters:Object):void {
            if(parameters.clusterize){
                var px:int = 0;
                var py:int = 0;
                var tot:int = 0;
                parameters.nodes.forEach(function(item:*, index:int, array:Array):void {
                    var ns:NodeSprite = nodes[item.id];
                    px += ns.x;
                    py += ns.y;
                    tot++;
                    removeNode(ns);
                });
                px = px/tot;
                py = py/tot;
                var cns:NodeSprite = buildNode(parameters.cluster, px, py);
                var ces:EdgeSprite = buildEdge(parameters.edge);
                operate();
            }
            else {
                var n:NodeSprite = nodes[parameters.cluster];
                if(buildNodes(cmdbData, n)) operate();
            }
        }

        public function updateCluster(cluster:Object):void {
            var ns:NodeSprite = nodes[cluster.id];
            ns.data.elements = cluster.elements;
            operate();
        }

        public function changeVisualization(visType:int):void {
            _os.index = visType;
            operate();
        }

        public function changeVisibility(newLevel:int):void {
            var dataVis:Data = visualization.data;
            var needsRedraw:Boolean = false;
            visualization.data.nodes.visit(function(ns:NodeSprite):void {
                if(ns.data.level > newLevel) {
                    removeNode(ns);
                    needsRedraw = true;
                }
            });

            if(needsRedraw) operate();
        }

        public function operate():void {
            setTimeout(function():void {
                var t:Transitioner = new Transitioner(duration);
                visualization.update(t).play();
            }, 200);
        }

        private function removeNode(n:NodeSprite):void {
            nodes[n.data.id] = null;
            n.visitEdges(function(es:EdgeSprite):void {
                var srcID:String = es.source.data.id;
                var trgID:String = es.target.data.id;
                var key:String = srcID + "_" + trgID;
                edges[key] = null;
                visualization.data.remove(es);
            });
            n.removeAllEdges();
            visualization.data.remove(n);
        }

        private function buildNodes(cmdbData:CmdbData, n:NodeSprite=null):Boolean {
            var dataVis:Data = visualization.data;
            var needsRedraw:Boolean = false;

            var posX:int = visualization.bounds.width / 2;
            var posY:int = visualization.bounds.height / 2;
            if(n != null) {
                posX = n.x;
                posY = n.y;
                removeNode(n);
            }

            // ---- NODES ----
            var i:int = 0;
            var dataNodes:Array = cmdbData.data.nodes();
            while(i < dataNodes.length) {
                var node:Object = dataNodes[i];
                i++;
                if( nodes[node.id] == null ) {
                    needsRedraw = true;
                    buildNode(node, posX, posY);
                }

            }

            // ---- CLUSTERS ----
            i = 0;
            var dataClusters:Array = cmdbData.data.clusters();
            while(i < dataClusters.length) {
                var cluster:Object = dataClusters[i];
                i++;
                if( nodes[cluster.id] == null ) {
                    needsRedraw = true;
                    buildNode(cluster);
                }
            }

            // ---- EDGES ----
            i = 0;
            var dataEdges:Array = cmdbData.data.edges();
            while(i < dataEdges.length) {
                var edge:Object = dataEdges[i];
                i++;
                var key:String = edge.source + "_" + edge.target;
                var e:EdgeSprite = null;
                if( edges[key] == null ) {
                    needsRedraw = true;
                    var es:EdgeSprite = buildEdge(edge);
                    if(n == null){
                        var src:NodeSprite = es.source;
                        var trg:NodeSprite = es.target;
                        if(src.x == 0) {
                            src.x = trg.x;
                            src.y = trg.y;
                        }
                        else if(trg.x == 0) {
                            trg.x = src.x;
                            trg.y = src.y;
                        }
                    }
                }
            }

            return needsRedraw;
        }

        private function buildNode(node:Object, posX:int = 0, posY:int = 0):NodeSprite {
            var ns:NodeSprite = visualization.data.addNode(node);
            if(node.level == 0) _root = ns;
            nodes[node.id] = ns;
            ns.x = posX;
            ns.y = posY;
            var rs:RectSprite = new RectSprite(-boxWidth/2, -boxHeight/2, boxWidth, boxHeight, 10, 10);
            if(node.type == "cluster") {
                var j:int;
                for (j = 1; j < 3; j++) {
                    var rs2:RectSprite = new RectSprite(-boxWidth/2 + (offset*(3-j)), -boxHeight/2 - (offset*(3-j)), boxWidth, boxHeight, 10, 10);
                    //rs2.fillColor = 0xAAFFBF60;
                    rs2.fillColor = _colorLevels[node.level][1];
                    rs2.lineColor = borderColor;
                    rs2.mouseEnabled = false;
                    rs2.mouseChildren = false;
                    ns.addChild(rs2);
                }
            }
            ns.addChild(rs);
            rs.lineColor = borderColor;

            if(node.level == 0) select(ns);

            rs.fillColor = _colorLevels[node.level][1];
            ns.mouseEnabled = false;
            ns.mouseChildren = false;
            ns.doubleClickEnabled = true;

            ns.addEventListener(MouseEvent.CLICK, function(evt:Event):void {
                var ens:NodeSprite = evt.currentTarget as NodeSprite;
                setTimeout(function():void {
                    select( ens );
                }, 100);
                dispatchEvent(new NodeEvent(NodeEvent.SELECT, ens.data));
            });

            if(node.type == "node") {
                ns.addEventListener(MouseEvent.DOUBLE_CLICK, function(evt:Event):void {
                    var ens:NodeSprite = evt.currentTarget as NodeSprite;
                    dispatchEvent(new NodeEvent(NodeEvent.CENTER, ens.data));
                });
            }

            return ns;
        }

        private function buildEdge(edge:Object):EdgeSprite {
            var src:NodeSprite = nodes[edge.source];
            var trg:NodeSprite = nodes[edge.target];
            var e:EdgeSprite = visualization.data.addEdgeFor(src, trg);
            e.data.domaindescription = edge.domaindescription;
            e.lineWidth = 2;
            e.buttonMode = true;
            edges[edge.source + "_" + edge.target] = e;
            return e;
        }

        public function select(n:NodeSprite):void {
            if(currentSelection){
                var rts1:RectSprite
                if(currentSelection.data.type == "node")
                    rts1 = (currentSelection.getChildAt(0)) as RectSprite;
                else
                    rts1 = (currentSelection.getChildAt(2)) as RectSprite;
                rts1.lineColor = borderColor;
                currentSelection.dirty();
            }
            var rts2:RectSprite
            if(n.data.type == "node")
                rts2 = (n.getChildAt(0)) as RectSprite;
            else
                rts2 = (n.getChildAt(2)) as RectSprite;
            rts2.lineColor = borderColorSelected;
            n.dirty();
            currentSelection = n;
        }
    }
}

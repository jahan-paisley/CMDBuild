package models
{
    import mx.collections.Sort;
    import mx.collections.ArrayCollection;

    import mx.utils.ObjectUtil;

    import utils.GraphUtil;

    public class DataStorage extends ArrayCollection {

        public function DataStorage() {
        }

        public function clear():void {
            this.source = new Array();
        }

        public function checkExistence(element:Object):int {
            var i:int;
            var arr:Array = this.source;
            while(i < arr.length) {
                if(matchCriteria(element, arr[i])) return i;
                i++;
            }
            return -1;
        }

        public function removeNodeById(id:String, removeEdges:Boolean = true):void {
            var pos:int = checkExistence({id: id});
            if(pos != -1) this.removeItemAt(pos);

            if( ! removeEdges ) return;

            this.filterFunction = function(value:Object):Boolean {
                return matchCriteria({type: "edge", target: id}, value) || matchCriteria({type: "edge", source: id}, value);
            };
            this.refresh();

            var end:int = this.length - 1
            for(var i:int = end; i >= 0; i--) {
                this.removeItemAt(i);
            }

            this.filterFunction = null;
            this.refresh();
        }

        public function merge(pos:int, node:Object):void {
            var local:Object = this.getItemAt(pos);
            for(var p:String in node) {
                if(node[p]) local[p] = node[p];
            }
        }

        public function nodes():Array {
            return getFilteredData({type: "node"});
        }

        public function edges():Array {
            return getFilteredData({type: "edge"});
        }

        public function clusters():Array {
            return getFilteredData({type: "cluster"});
        }

        public function getFilteredData(criteria:Object):Array {
            var rv:Array = [];
            this.filterFunction = function(value:Object):Boolean {
                return matchCriteria(criteria, value);
            };
            this.refresh();
            for(var i:int =  0; i < this.length; i++) {
                rv.push( this.getItemAt(i) );
            }
            this.filterFunction = null;
            this.refresh();
            return rv;
        }

        public function getNodesForCluster(relation:Object):Array {
            var keyParent:String = GraphUtil.createNodeIdFromRelation(relation);
            var edges:Array = getFilteredData({target: keyParent, type: "edge"});
            var nodes:Array = [];
            edges.forEach(function(item:*, index:int, array:Array):void {
                var tok:Array = item.source.split("_")
                nodes.push({classId: tok[1], objId: tok[2]});
            });
            return nodes;
        }

        public function clusteringRequestFor(relation:Object):Object { // parentClassId:int, parentObjId:int, childClassId:int
            // search edges connected to parent node and to a child of class childClassId
            var keyParent:String = GraphUtil.createNodeIdFromRelation(relation);
            this.filterFunction = function(value:Object):Boolean {
                var keyChild:RegExp = new RegExp("node_" + relation.childClassId + "_[0-9]+", "i");
                return matchCriteria({source: keyParent, type: "edge"}, value) && (value.target.match(keyChild) != null);
            };
            this.refresh();
            var edgesTarget:Array = [];
            // store them and remove from structure, to do this we need to loop backward
            var end:int = this.length - 1
            var domaindescription:String = this.getItemAt(0).domaindescription;
            for(var i:int = end; i >= 0; i--) {
                var item:Object = this.removeItemAt(i);
                edgesTarget.push(item.target);
            }

            // search nodes connected to these edges
            this.filterFunction = function(value:Object):Boolean {
                if( matchCriteria({ type: "node", level: relation.level, classId: relation.childClassId}, value) ){
                    return edgesTarget.indexOf(value.id) != -1;
                }
                return false;
            };
            this.refresh();
            var nodesToRemove:Array = [];
            // store them and remove from structure, to do this we need to loop backward
            end = this.length - 1
            for(i = end; i >= 0; i--) {
                nodesToRemove.push( this.removeItemAt(i) );
            }

            // Add cluster node and edge
            var cluster:Object = {
                type: "cluster",
                id: GraphUtil.createClusterIdFromRelation(relation),
                classId: relation.childClassId,
                level: relation.level,
                classDesc: nodesToRemove[0].classDesc,
                elements: relation.elements };
            var cedge:Object = {source: keyParent, target: cluster.id, domaindescription: domaindescription};
            this.addItem(cluster);
            this.addItem(cedge);

            // clear selection
            this.filterFunction = null;
            this.refresh();

            return {nodes: nodesToRemove, cluster: cluster, edge: cedge};
        }

        private function matchCriteria(criteria:Object, obj:Object):Boolean {
            for(var p:String in criteria) {
                if(criteria[p] != obj[p]) return false;
            }
            return true
        }

    }
}

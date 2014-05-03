package utils
{
    import mx.utils.ObjectUtil;

    public class GraphUtil {

        private static var _parameters:Object = {
            baseLevel: 1,
            clusteringThreshold: 5,
            expandingThreshold: 20,
            extensionMinimum: 1,
            extensionMaximum: 4
        }

        public static function set parameters(parameters:Object):void {
            if(parameters.baseLevel)
                _parameters.baseLevel = parseInt(parameters.baseLevel);
            if(parameters.clusteringThreshold)
                _parameters.clusteringThreshold = parseInt(parameters.clusteringThreshold);
            if(parameters.expandingThreshold)
                _parameters.expandingThreshold = parseInt(parameters.expandingThreshold);
            if(parameters.extensionMaximum)
                _parameters.extensionMaximum = parseInt(parameters.extensionMaximum);
        }

        public static function get parameters():Object {
            return _parameters;
        }

        public static function createClusterIdFromRelation(relation:Object):String {
            return "cluster_" + relation.parentClassId + "_" + relation.parentObjId + "_" + relation.domainId + "_" + relation.childClassId;
        }

        public static function createNodeIdFromRelation(relation:Object):String {
            return "node_" + relation.parentClassId + "_" + relation.parentObjId;
        }

        public static function createIdFromData(obj:Object):String {
            if(obj.type == "node")
                return "node_" + obj.classId + "_" + obj.objId;
            else if(obj.type == "cluster")
                return "cluster_" + obj.parentClassId + "_" + obj.parentObjId + "_" + obj.domainId;
            return "";
        }

    }
}

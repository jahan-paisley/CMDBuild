package models.events
{
    import flash.events.Event;

    public class CmdbDataEvent extends Event
    {
        public static const CLEAR:String = "clear";
        public static const NEW_DATA:String = "new_data";
        public static const CARD:String = "card";
        public static const FILTER:String = "filter";
        public static const RELATION_UPDATE:String = "relation_update";
        public static const CLUSTER:String = "cluster";
        public static const ERROR:String = "error";

        private var _parameters:Object = {};

        public function get parameters():Object { return _parameters; }

        public function CmdbDataEvent(type:String, parameters:Object = null)
        {
            super(type);
            if(parameters) _parameters = parameters;
        }
    }
}
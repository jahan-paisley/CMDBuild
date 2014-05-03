package widgets.events
{
    import flash.events.Event;

    public class ClusterEvent extends Event
    {
        public static const CLUSTERIZE:String = "clusterize";

        private var _clusterize:Boolean;
        private var _relation:Object;

        public function get clusterize():Boolean { return _clusterize; }
        public function get relation():Object  { return _relation; }

        public function ClusterEvent(clusterize:Boolean, relation:Object)
        {
            super(CLUSTERIZE);
            _clusterize  = clusterize;
            _relation = relation;
        }
    }
}
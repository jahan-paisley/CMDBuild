package widgets.events
{
    import flash.events.Event;

    public class NodeEvent extends Event
    {
        public static const SELECT:String = "node_select";
        public static const CENTER:String = "node_center";

        private var _node:Object;

        public function get node():Object { return _node; }

        public function NodeEvent(type:String, node:Object)
        {
            super(type);
            _node = node;
        }
    }
}
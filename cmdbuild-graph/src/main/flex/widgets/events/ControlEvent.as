package widgets.events
{
    import flash.events.Event;

    public class ControlEvent extends Event
    {
        public static const VIS_TYPE:String  = "vis_type";
        public static const EXTENSION:String = "extension";

        private var _visType:Number;
        private var _extension:Number;

        public function get visType():Number { return _visType; }
        public function get extension():Number { return _extension; }

        public function ControlEvent(type:String, visType:Number, extension:Number)
        {
            super(type);
            _visType = visType;
            _extension = extension;
        }
    }
}
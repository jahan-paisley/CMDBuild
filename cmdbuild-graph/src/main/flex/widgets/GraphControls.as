package widgets
{
    import flash.events.Event;

    import mx.containers.HBox;
    import mx.controls.ComboBox;
    import mx.controls.Button;
    import mx.controls.Label;
    import mx.controls.NumericStepper;
    import mx.controls.Spacer;
    import mx.collections.ArrayCollection;
    import mx.events.ListEvent;
    import mx.events.NumericStepperEvent;

    import widgets.events.ControlEvent;

    import utils.GraphUtil;

    [Event(name="vis_type",  type="widgets.events.ControlEvent")]
    [Event(name="extension", type="widgets.events.ControlEvent")]

    public class GraphControls extends HBox
    {
        private var _labelT:Label = new Label();
        private var _labelE:Label = new Label();
        private var _visType:ComboBox = new ComboBox();
        private var _extension:NumericStepper = new NumericStepper();

        [Bindable]
        public var _visTypeData:ArrayCollection = new ArrayCollection(
            [ {label:"RadialTree",    data:0},
              {label:"Circle",        data:1} ]);

        public function set parameters(p:Object):void {
            _extension.minimum = p.extensionMinimum;
            _extension.maximum = p.extensionMaximum;
            _extension.value = p.baseLevel;
        }

        public override function initialize():void {
            super.initialize();

            _labelT.text = "Tipo";
            addChild(_labelT);

            _visType.dataProvider = _visTypeData;

            addChild(_visType);

            var spacer:Spacer = new Spacer();
            spacer.percentWidth = 100;
            addChild(spacer);

            _labelE.text = "Estensione";
            addChild(_labelE);

            // Set properties
            _extension.minimum = 1;
            _extension.maximum = 4;
            _extension.stepSize = 1;
            _extension.value = 1;

            addChild(_extension);

            _visType.addEventListener(ListEvent.CHANGE, function(event:Event):void {
                trace("changed vis");
                dispatchEvent(new ControlEvent(
                    ControlEvent.VIS_TYPE,
                    _visType.selectedItem.data,
                    -1));
            });
            _extension.addEventListener(NumericStepperEvent.CHANGE , function(event:Event):void {
                trace("changed level");
                dispatchEvent(new ControlEvent(
                    ControlEvent.EXTENSION,
                    -1,
                    _extension.value));
            });
        }
    }

}
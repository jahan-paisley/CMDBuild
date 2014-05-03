package extensions
{
    import flash.events.MouseEvent;

    import mx.controls.DataGrid;

    import extensions.CenteredCheckBox;

    import widgets.events.ClusterEvent;

    import mx.utils.ObjectUtil;

    import utils.GraphUtil;

    public class ClusterCheckBox extends CenteredCheckBox
    {
        override protected function clickHandler(event:MouseEvent):void
        {
            super.clickHandler(event);
            DataGrid(listData.owner).dispatchEvent(new ClusterEvent(selected, data));
        }

        override protected function updateDisplayList(w:Number, h:Number):void
        {
            this.enabled = (data.elements > 1 && data.elements <= GraphUtil.parameters.expandingThreshold);
            super.updateDisplayList(w,h);
        }

    }
}
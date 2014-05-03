package extensions
{
    import flash.display.DisplayObject;
    import flash.events.MouseEvent;
    import flash.text.TextField;

    import mx.controls.CheckBox;
    import mx.controls.dataGridClasses.DataGridListData;
    import mx.controls.DataGrid;

    import widgets.events.ClusterEvent;

    public class CenteredCheckBox extends CheckBox
    {
        // this function is defined by mx.controls.CheckBox
        // it is the default handler for its click event
        override protected function clickHandler(event:MouseEvent):void
        {
            super.clickHandler(event);
            // this is the important line as it updates the data field that this CheckBox is rendering
            data[DataGridListData(listData).dataField] = selected;
        }

        // center the checkbox icon
        override protected function updateDisplayList(w:Number, h:Number):void
        {
            super.updateDisplayList(w, h);

            var n:int = numChildren;
            for (var i:int = 0; i < n; i++)
            {
                var c:DisplayObject = getChildAt(i);
                // CheckBox component is made up of icon skin and label TextField
                // we ignore the label field and center the icon
                if (!(c is TextField))
                {
                    c.x = Math.round((w - c.width) / 2);
                    c.y = Math.round((h - c.height) / 2);
                }
            }
        }

    }
}
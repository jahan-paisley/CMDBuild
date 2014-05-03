package widgets
{
    import mx.controls.DataGrid;
    import mx.collections.ArrayCollection;

    import models.events.CmdbDataEvent;

    public class Card extends DataGrid
    {
        private var _card:ArrayCollection = new ArrayCollection();

        public function set mode(newMode:String):void {
            if(newMode == CmdbDataEvent.CARD) {
                editable = false;
                columns[0].editable = false;
                columns[1].editable = false;
            }
            else if(newMode == CmdbDataEvent.FILTER){
                editable = true;
                columns[0].editable = false;
                columns[1].editable = true;
            }
            invalidateProperties();
        }

        public function Card(){
            dataProvider = _card;
        }

    }

}
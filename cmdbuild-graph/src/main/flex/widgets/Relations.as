package widgets
{

    import mx.controls.DataGrid;
    import mx.collections.ArrayCollection;

    import mx.utils.ObjectUtil;

    import utils.GraphUtil;

    public class Relations extends DataGrid
    {
        public function selectRelation(node:Object):void {
            for(var i:int = 0; i < dataProvider.length; i++) {
                var item:Object = dataProvider.getItemAt(i);
                if( (node.type == "node" && node.classId == item.childClassId && node.level == item.level) ||
                (node.type == "cluster" && GraphUtil.createClusterIdFromRelation(item) == node.id) ) {
                    selectedIndex = i;
                    scrollToIndex(i);
                    return;
                }
            }
            selectedIndex = -1;
            scrollToIndex(0);
        }
    }

}
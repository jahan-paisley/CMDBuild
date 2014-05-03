package extensions {

import flare.animate.Transitioner;
import flare.display.RectSprite;

import flare.display.TextSprite;
import flare.vis.data.EdgeSprite;
import flare.vis.operator.Operator;

public class EdgeLabelLayout extends Operator {
    private var _t:Transitioner;

    public function EdgeLabelLayout()
    {
        super();
    }

    /** @inheritDoc */
    public override function operate(t:Transitioner=null):void
    {
        _t = (t!=null ? t : Transitioner.DEFAULT);

        updateEdgeLabels(_t);
    }

    /**
    * Updates all edge labels.
    *
    * @param t a transitioner to collect value updates
    */
    public function updateEdgeLabels(t:Transitioner=null):void
    {
        // Visit edges
        visualization.data.edges.visit(function(e:EdgeSprite):void
        {
            var els:EdgeLabelSprite = e.getChildByName(EdgeLabelSprite.EDGELABELNAME) as EdgeLabelSprite;

            var xmid:Number = (e.x1+e.x2)/2;
            var ymid:Number = (e.y1+e.y2)/2;

            els.x = xmid - els.width/2;
            els.y = ymid - els.height / 2;
        });
    }

}

}
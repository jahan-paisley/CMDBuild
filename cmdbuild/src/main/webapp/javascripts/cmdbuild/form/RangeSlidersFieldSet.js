(function() {
	Ext.define("CMDBuild.RangeSlidersFieldSet", {
		extend: "Ext.container.Container",

		maxSliderField: undefined,
		minSliderField: undefined,

		initComponent: function() {
			if (!this.maxSliderField) {
				throw new Error("You must assign a maxSliderField to the RangeSliderFieldset");
			}

			if (!this.minSliderField) {
				throw new Error("You must assign a minSliderField to the RangeSliderFieldset");
			}


			this.items = [this.minSliderField,this.maxSliderField];

			this.callParent(arguments);

			handleDisableEvent(this);
			handleMaxSliderEvents(this);
			handleMinSliderEvents(this);
		},

		disable: function() {
			this.maxSliderField.disable();
			this.minSliderField.disable();
		},

		enable: function() {
			this.maxSliderField.enable();
			this.minSliderField.enable();
		}
	});

	function handleMaxSliderEvents(rangeSlider) {
		rangeSlider.maxSliderField.on("dragstart", function(slider, event) {
			slider.startDragValue = slider.getValue();
		});
		
		rangeSlider.maxSliderField.on("dragend", function(slider, event) {
			if (slider.getValue() < rangeSlider.minSliderField.getValue()) {
				slider.setValue(rangeSlider.minSliderField.getValue());
			}
			slider.startDragValue = undefined;
		});
	};
	
	function handleMinSliderEvents(rangeSlider) {
		rangeSlider.minSliderField.on("dragstart", function(slider, event) {
			slider.startDragValue = slider.getValue();
		});
		
		rangeSlider.minSliderField.on("dragend", function(slider, event) {
			if (slider.getValue() > rangeSlider.maxSliderField.getValue()) {
				slider.setValue(rangeSlider.maxSliderField.getValue());
			}
			slider.startDragValue = undefined;
		});
	};
	
	function handleDisableEvent(rangeSlider) {
		rangeSlider.on("enable", function(){
			rangeSlider.maxSliderField.enable();
			rangeSlider.minSliderField.enable();
		});
		rangeSlider.on("disable", function(){
			rangeSlider.maxSliderField.disable();
			rangeSlider.minSliderField.disable();
		});
	};
})();
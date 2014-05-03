
/* global Ext
 *
 *
 ************************************************************************************
 *   This file is distributed on an AS IS BASIS WITHOUT ANY WARRANTY;
 *   without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 ************************************************************************************

 License: ux.Media classes are licensed under the terms of
 the Open Source GPL 3.0 license (details: http://www.gnu.org/licenses/gpl.html).

 Donations are welcomed: http://donate.theactivegroup.com

 Commercial use is prohibited without a Commercial License. See http://licensing.theactivegroup.com.

 Notes: the <embed> tag is NOT used(or necessary) in this implementation

 Version   2.2  11/12/2009
          Adds: Ext 3.1 Compatibility
 Version   2.1  11/11/2008
          Fixes:
            Corrects missing unsupportedText markup rendering.
            Corrects inline markup rendering.
            Corrects autoSize height/width macro replacement.
            Corrects mixed content warnings on SSL sites.
          Adds:
            loaded readyState detection for all media types and sub-classes.

 Version:  2.0
           Height/Width now honors inline style as well,
           Added Component::mediaEl(eg: 'body', 'el') for targeted media rendering.
           Added scale and status macros.
           Added px unit assertion for strict DTDs.
           Final Quicktime config.
           Adds new PDF(Iframe), Remote Desktop Connection, Silverlight, Office Web Connect-XLS (IE),
                Powerpoint, Wordpress player mediaType profiles.


 Version:  Rc1
           Adds inline media rendering within markup: <div><script>document.write(String(new Ext.ux.Media(mediaCfg)));</script></div>
           New extensible classes :
              ux.Media
              ux.MediaComponent
              ux.MediaPanel

           Solves the Firefox reinitialization problem for Ext.Components with embedded <OBJECT> tags
           when the upstream DOM is reflowed.

           See Mozilla https://bugzilla.mozilla.org/show_bug.cgi?id=262354

 Version:  .31 Fixes to canned WMV config.
 Version:  .3  New class Heirarchy.  Adds renderMedia(mediaCfg) method for refreshing
               a mediaPanels body with a new/current mediaCfg.
 Version:  .2  Adds JW FLV Player Support and enhances mediaClass defaults mechanism.
 Version:  .11 Modified width/height defaults since CSS does not seem to
                honor height/width rules
 Version:  .1  initial release

 mediaCfg: {Object}
     { mediaType : mediaClass defined by ux.Media.mediaTypes[mediaClass]
      ,url       : Url resource to load when rendered
      ,requiredVersion : may specify a specific player/plugin version (for use with inline plugin updates where implemented)
      ,loop      : (true/false) (@macro enabled)
      ,scripting : (true/false) (@macro enabled)
      ,start     : (true/false) (@macro enabled)
      ,volume    : (number%, default: 20 ) audio volume level % (@macro enabled)
      ,height    : (default: 100%) (@macro enabled)
      ,width     : (default: 100%) (@macro enabled)
      ,scale     : (default: 1) (@macro enabled)
      ,status    : (default: false) (@macro enabled)
      ,autoSize  : (true/false) If true the rendered <object> consumes 100% height/width of its
                     containing Element.  Actual container height/width are available to macro substitution
                     engine.
      ,controls  : optional: show plugins control menu (true/false) (@macro enabled)
      ,unsupportedText: (String,DomHelper cfg) Text to render if plugin is not installed/available.
      ,listeners  : {"mouseover": function() {}, .... } DOM listeners to set each time the Media is rendered.
      ,params   : { }  members/values unique to Plugin provider
     }

*/

(function(){

    //remove null and undefined members from an object and optionally URL encode the results
    var compactObj =  function(obj, encodeIt){
            var out = obj && Ext.isObject(obj)? {} : obj;
            if(out && Ext.isObject(out)){
	            for (var member in obj){
	               (obj[member] === null || obj[member] === undefined) || (out[member] = obj[member]);
	            }
            }
            return encodeIt ? 
                 ((out && Ext.isObject(out)) ? Ext.urlEncode(out) : encodeURI(out))
                 : out;
        },
        toString = Object.prototype.toString;
        
    /**
     * plugin detection namespace for VisibilityMode fixes
     */
    Ext.ns('Ext.ux.plugin');
    
    
   /**
    *
    * @class Ext.ux.Media
    * @version 2.2
    * @author Doug Hendricks. doug[always-At]theactivegroup.com
    * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
    * @constructor
    * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
    * @desc
    * Base Media Class
    * Used primarily for rendering a mediaCfg for use with inline markup.
    */

    Ext.ux.Media = function(config){
         this.toString = this.asMarkup;  //Inline rendering support for this and all subclasses
         Ext.apply(this,config||{});
         this.initMedia();
    };
    var ux = Ext.ux.Media,
        stateRE = /4$/i;

    if(parseFloat(Ext.getVersion()) < 2.2){ throw "Ext.ux.Media and sub-classes are not License-Compatible with your Ext release.";}

    Ext.ux.Media.prototype = {
        
         hasVisModeFix : !!Ext.ux.plugin.VisibilityMode, 
         /**
         * @property {Object} mediaObject An {@link Ext.ux.Media.Element} reference to rendered DOM Element.
         */
         mediaObject     : null,

         /**
          * @cfg {Object} mediaCfg Media configuration options.
          * @example mediaCfg  : {
             id    : String   Desired DOM id of rendered Media tag
             tag   :
             style : Obj  optional DomHelper style object

            }
         */
         mediaCfg        : null,
         mediaVersion    : null,
         requiredVersion : null,
         
         /**
          * @cfg {String} hideMode  <p>Note: If the ux.VisibilityMode plugin is available, a value of 'nosize' will activate
          * the plugin to prevent DOM reflow from re-initializing the rendered media.
          */
         hideMode        : 'display',

         /**
          * @cfg {String/DOMHelperObject} unsupportedText Text Markup/DOMHelper config displayed when the media is not available or cannot be rendered without an additional browser plugin.
          */
         unsupportedText : null,

         animCollapse  :  Ext.enableFx && Ext.isIE,

         animFloat     :  Ext.enableFx && Ext.isIE,

         autoScroll    : true,

         bodyStyle     : {position: 'relative'},

        /**
          * @private (usually called once by initComponent)
          * Subclasses should override for special startup tasks
          */
         initMedia      : function(){
            this.hasVisModeFix = !!Ext.ux.plugin.VisibilityMode; 
         },

         /**
          * @cfg {boolean} disableCaching Disable browser caching of URLs
          */
         disableCaching  : false,

         _maxPoll        : 200,

         /** @private */
         getMediaType: function(type){
             return ux.mediaTypes[type];
         },

         /** @private
          Assert default values and exec as functions
          */
         assert : function(v,def){
              v= typeof v === 'function'?v.call(v.scope||null):v;
              return Ext.value(v ,def);
         },

        /** @private
           Assert/cleanse ID.  Overridable by sub-classes
         */
         assertId : function(id, def){
             id || (id = def || Ext.id());
             return id;
         },

        /** @private
         * Prepare a URL for disabledCaching
         */
         prepareURL : function(url, disableCaching){
            var parts = url ? url.split('#') : [''];
            if(!!url && (disableCaching = disableCaching === undefined ? this.disableCaching : disableCaching) ){
                var u = parts[0];
                if( !(/_dc=/i).test(u) ){
                    var append = "_dc=" + (new Date().getTime());
                    if(u.indexOf("?") !== -1){
                        u += "&" + append;
                    }else{
                        u += "?" + append;
                    }
                    parts[0] = u;
                }
            }
            return parts.length > 1 ? parts.join('#') : parts[0];
         },

          /* Normalize the mediaCfg to DOMHelper cfg */
         prepareMedia : function(mediaCfg, width, height, ct){

             mediaCfg = mediaCfg ||this.mediaCfg;

             if(!mediaCfg){return '';}

             var m= Ext.apply({url:false,autoSize:false}, mediaCfg); //make a copy

             m.url = this.prepareURL(this.assert(m.url,false),m.disableCaching);

             if( m.mediaType){

                 var value,tag, p, El = Ext.Element.prototype;
                 var media = Ext.apply({}, this.getMediaType(this.assert(m.mediaType,false)) || false );
                 var params = compactObj(Ext.apply(media.params||{},m.params || {}));
                 for(var key in params){

                    if(params.hasOwnProperty(key)){
                      m.children || (m.children = []);
                      p = this.assert(params[key],null);
                      p && (p = compactObj(p, m.encodeParams !== false));
                      tag = 
                        {tag:'param'
                         ,name:key
                         ,value: p 
                       };
                       (tag.value == key) && delete tag.value;
                       p && m.children.push(tag);

                    }
                 }
                 delete   media.params;

                 //childNode Text if plugin/object is not installed.
                 var unsup = this.assert(m.unsupportedText|| this.unsupportedText || media.unsupportedText,null);
                 if(unsup){
                     m.children || (m.children = []);
                     m.children.push(unsup);
                 }

                 if(m.style && typeof m.style != "object") { throw 'Style must be JSON formatted'; }

                 m.style = this.assert(Ext.apply(media.style || {}, m.style || {}) , {});
                 delete media.style;

                 m.height = this.assert(height || m.height || media.height || m.style.height, null);
                 m.width  = this.assert(width  || m.width  || media.width || m.style.width ,null);

                 m = Ext.apply({tag:'object'},m,media);

                 //Convert element height and width to inline style to avoid issues with display:none;
                 if(m.height || m.autoSize)
                 {
                    Ext.apply(m.style, {
                        //Ext 2 & 3 compatibility -- Use the defaultUnit from the Component's el for default
                      height:(Ext.Element.addUnits || El.addUnits).call(this.mediaEl, m.autoSize ? '100%' : m.height ,El.defaultUnit||'px')});
                 }
                 if(m.width || m.autoSize)
                 {
                    Ext.apply(m.style, {
                        //Ext 2 & 3 compatibility -- Use the defaultUnit from the Component's el for default
                      width :(Ext.Element.addUnits || El.addUnits).call(this.mediaEl, m.autoSize ? '100%' : m.width ,El.defaultUnit||'px')});
                 }

                 m.id   = this.assertId(m.id);
                 m.name = this.assertId(m.name, m.id);

                 m._macros= {
                   url       : m.url || ''
                  ,height    : (/%$/.test(m.height)) ? m.height : parseInt(m.height,10)||null
                  ,width     : (/%$/.test(m.width)) ? m.width : parseInt(m.width,10)||null
                  ,scripting : this.assert(m.scripting,false)
                  ,controls  : this.assert(m.controls,false)
                  ,scale     : this.assert(m.scale,1)
                  ,status    : this.assert(m.status,false)
                  ,start     : this.assert(m.start, false)
                  ,loop      : this.assert(m.loop, false)
                  ,volume    : this.assert(m.volume, 20)
                  ,id        : m.id
                 };

                 delete   m.url;
                 delete   m.mediaType;
                 delete   m.controls;
                 delete   m.status;
                 delete   m.start;
                 delete   m.loop;
                 delete   m.scale;
                 delete   m.scripting;
                 delete   m.volume;
                 delete   m.autoSize;
                 delete   m.autoScale;
                 delete   m.params;
                 delete   m.unsupportedText;
                 delete   m.renderOnResize;
                 delete   m.disableCaching;
                 delete   m.listeners;
                 delete   m.height;
                 delete   m.width;
                 delete   m.encodeParams;
                 return m;
              }else{
                 var unsup = this.assert(m.unsupportedText|| this.unsupportedText || media.unsupportedText,null);
                 unsup = unsup ? Ext.DomHelper.markup(unsup): null;
                 return String.format(unsup || 'Media Configuration/Plugin Error',' ',' ');
             }
           },

           /**
            * @return {String} the renderable markup of a passed normalized mediaCfg
            */
         asMarkup  : function(mediaCfg){
              return this.mediaMarkup(this.prepareMedia(mediaCfg));
         },

          /** @private
          * macro replacement engine for mediaCfg -> rendered tags/styling
          */
         mediaMarkup : function(mediaCfg){
            mediaCfg = mediaCfg || this.mediaCfg;
            if(mediaCfg){
                 var _macros = mediaCfg._macros;
                 delete mediaCfg._macros;
                 var m = Ext.DomHelper.markup(mediaCfg);
                 if(_macros){
                   var _m, n;
                    for ( n in _macros){
                      _m = _macros[n];
                      if(_m !== null){
                           m = m.replace(new RegExp('((%40|@)'+n+')','g'),_m+'');
                      }
                    }
                  }
                  
                  return m;
            }
         },

         /** @private
         * Set the mediaMask if defined
         */
         setMask  : function(el) {
             var mm;
             if((mm = this.mediaMask)){
                    mm.el || (mm = this.mediaMask = new Ext.ux.IntelliMask(el,Ext.isObject(mm) ? mm : {msg : mm}));
                    mm.el.addClass('x-media-mask');
             }

         },
         /**
         *  Refreshes the Media Object based on last known mediaCfg.
         *  @param {Element} target The target container Element for the refresh operation.
         *  @returns {Ext.ux.Media} this
         */
          refreshMedia  : function(target){
                 if(this.mediaCfg) {this.renderMedia(null,target);}
                 return this;
          },

          /**
          *  This method updates the target Element with a new mediaCfg object,
          *  or supports a refresh of the target based on the current mediaCfg object
          *  This method may be invoked inline (in Markup) before the DOM is ready
          *  param position indicate the DomHeper position for Element insertion (ie 'afterbegin' the default)
          */
          renderMedia : function(mediaCfg, ct, domPosition , w , h){
              if(!Ext.isReady){
                  Ext.onReady(this.renderMedia.createDelegate(this,Array.prototype.slice.call(arguments,0)));
                  return;
              }
              var mc = (this.mediaCfg = mediaCfg || this.mediaCfg) ;
              ct = Ext.get(this.lastCt || ct || (this.mediaObject?this.mediaObject.dom.parentNode:null));
              this.onBeforeMedia.call(this, mc, ct, domPosition , w , h);
              
              if(ct){
                  this.lastCt = ct;
                  if(mc && (mc = this.prepareMedia(mc, w, h, ct))){
                     this.setMask(ct);
                     this.mediaMask && this.autoMask && this.mediaMask.show();
                     this.clearMedia().writeMedia(mc, ct, domPosition || 'afterbegin');
                  }
              }
              this.onAfterMedia(ct);
          },

          /** @private
           *Override if necessary to render to targeted container
           */
          writeMedia : function(mediaCfg, container, domPosition ){
              var ct = Ext.get(container), markup;
              if(ct){
                markup = this.mediaMarkup(mediaCfg)
                domPosition ? Ext.DomHelper.insertHtml(domPosition, ct.dom, markup)
                  :ct.update(markup);
              }
          },

          /**
           * Remove a rendered  mediaObject from the DOM.
           */
          clearMedia : function(){
            var mo;
            if(Ext.isReady && (mo = this.mediaObject)){
                mo.remove(true,true);
            }
            this.mediaObject = null;
            return this;
          },

           /** @private (deprecated*/
          resizeMedia   : function(comp, aw, ah, w, h){
              var mc = this.mediaCfg;
              if(mc && this.rendered && mc.renderOnResize && (!!aw || !!ah)){
                  // Ext.Window.resizer fires this event a second time
                  if(arguments.length > 3 && (!this.mediaObject || mc.renderOnResize )){
                      this.refreshMedia(this[this.mediaEl]);
                  }
              }
          },

          /** @private */
          onBeforeMedia  : function(mediaCfg, ct, domPosition, width, height){

            var m = mediaCfg || this.mediaCfg, mt;

            if( m && (mt = this.getMediaType(m.mediaType)) ){
                m.autoSize = m.autoSize || mt.autoSize===true;
                var autoSizeEl;
                //Calculate parent container size for macros (if available)
                if(m.autoSize && (autoSizeEl = Ext.isReady?
                    //Are we in a layout ? autoSize to the container el.
                     Ext.get(this[this.mediaEl] || this.lastCt || ct) :null)
                 ){
                  m.height = this.autoHeight ? null : autoSizeEl.getHeight(true);
                  m.width  = this.autoWidth ? null : autoSizeEl.getWidth(true);
                }

             }
             this.assert(m.height,height);
             this.assert(m.width ,width);
             mediaCfg = m;

          },

          /** @private
           * Media Load Handler, called when a mediaObject reports a loaded readystate
           */
          onMediaLoad : function(e){
               if(e && e.type == 'load'){
                  this.fireEvent('mediaload',this, this.mediaObject );
                  this.mediaMask && this.autoMask && this.mediaMask.hide();
               }
          },
          /** @private */
          onAfterMedia   : function(ct){
               var mo;
               if(this.mediaCfg && ct && 
                  (mo = new (this.elementClass || Ext.ux.Media.Element)(ct.child('.x-media', true),true )) &&
                   mo.dom
                  ){
                   //Update ElCache with the new Instance
                   this.mediaObject = mo;
                   mo.ownerCt = this;

                   var L; //Reattach any DOM Listeners after rendering.
                   if(L = this.mediaCfg.listeners ||null){
                      mo.on(L);  //set any DOM listeners
                    }
                   this.fireEvent('mediarender',this, this.mediaObject );

                    //Load detection for non-<object> media (iframe, img)
                   if(mo.dom.tagName !== 'OBJECT'){
                      mo.on({
                       load  :this.onMediaLoad
                      ,scope:this
                      ,single:true
                     });
                   } else {
                       //IE, Opera possibly others, support a readyState on <object>s
                       this._countPoll = 0;
                       this.pollReadyState( this.onMediaLoad.createDelegate(this,[{type:'load'}],0));
                   }
               }
              (this.autoWidth || this.autoHeight) && this.syncSize();
          },

          /**
           * @private
           * synthesize a mediaload event for DOMs that support object.readyState
           */
         pollReadyState : function( cb, readyRE){

            var media = this.getInterface();
            if(media && 'readyState' in media){
                (readyRE || stateRE).test(media.readyState) ? cb() : arguments.callee.defer(10,this,arguments);
            }
         },

          /**
          * @return {HTMLElement} reference to the rendered media DOM Element.
          */
          getInterface  : function(){
              return this.mediaObject?this.mediaObject.dom||null:null;
          },

         detectVersion  : Ext.emptyFn,

         /**
            @cfg {Boolean} autoMask
            @default false
            Class default: IE provides sufficient DOM readyState for object tags to manage {@link #mediaMask}s automatically
            (No other browser does), so masking must either be directed manually or use the autoHide option
            of the {@link Ext.ux.IntelliMask}.
          * <p>If true the Component attempts to manage the mediaMask based on events/media status,
          * false permits control of the mask visibility manually.
          */

         autoMask   : false
    };

    var componentAdapter = {

        init         : function(){

            this.getId = function(){
                return this.id || (this.id = "media-comp" + (++Ext.Component.AUTO_ID));
            };

            this.html = this.contentEl = this.items = null;
           
            this.initMedia();
             
            //Attach the Visibility Fix (if available) to the current instance
            if(this.hideMode == 'nosize' && this.hasVisModeFix ){
                  new Ext.ux.plugin.VisibilityMode({ 
                      elements: ['bwrap','mediaEl'],
                      hideMode:'nosize'}).init(this); 
            } 

            //Inline rendering support for this and all subclasses
            this.toString = this.asMarkup;

            this.addEvents(

              /**
                * Fires immediately after the markup has been rendered.
                * @event mediarender
                * @memberOf Ext.ux.Media
                * @param {Object} component This Media Class object instance.
                * @param {Element} mediaObject The Ext.Element object rendered.
               */
                'mediarender',
               /**
                * Fires when the mediaObject has reported a loaded state (IE, Opera Only)
                * @event mediaload
                * @memberOf Ext.ux.Media
                * @param {Object} component This Media Class object instance.
                * @param {Element} mediaObject The Ext.Element object loaded.
                */

                'mediaload');

        },
        
        afterRender  : function(ct){
            //set the mediaMask
            this.setMask(this[this.mediaEl] || ct);
            componentAdapter.setAutoScroll.call(this);
            this.renderMedia(this.mediaCfg, this[this.mediaEl]);
        },
        /**
         * @private
         */
        beforeDestroy  :  function(){
            this.clearMedia();
            Ext.destroy(this.mediaMask, this.loadMask);
            this.lastCt = this.mediaObject = this.renderTo = this.applyTo = this.mediaMask = this.loadMask = null;
        },
         /** @private */
        setAutoScroll   : function(){
            if(this.rendered){
                this.getContentTarget().setOverflow(!!this.autoScroll ? 'auto':'hidden');
            }
        },
        
        getContentTarget : function(){
            return this[this.mediaEl];
        },
        
        onResize : function(){
            if(this.mediaObject && this.mediaCfg.renderOnResize){
                this.refreshMedia();
            }
        }
    };

    /**
     * @class Ext.ux.Media.Component
     * @extends Ext.BoxComponent
     * @version 2.2
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     * @base Ext.ux.Media
     * @constructor
     * @param {Object} config The config object
     */

    Ext.define("Ext.ux.Media.Component", {
        extend: "Ext.BoxComponent",
        alias: "uxmedia",
        ctype: "Ext.ux.Media.Component",

        /**
        * @cfg {String} mediaEl The name of the containing element for the media.
        * @default 'el'
        */
        mediaEl         : 'el',
        
        autoScroll    : true,

        autoEl  : {tag:'div',style : { overflow: 'hidden', display:'block',position: 'relative'}},

        cls     : "x-media-comp",

        mediaClass    : Ext.ux.Media,
        constructor   : function(config){
          //Inherit the ux.Media class
          Ext.apply(this , config, this.mediaClass.prototype );
          ux.Component.superclass.constructor.apply(this, arguments);
        },
        /** @private */
        initComponent   : function(){
            ux.Component.superclass.initComponent.apply(this,arguments);
            componentAdapter.init.apply(this,arguments);
        },
        /** @private */
        afterRender  : function(ct){
            ux.Component.superclass.afterRender.apply(this,arguments);
            componentAdapter.afterRender.apply(this,arguments);
         },
         /** @private */
        beforeDestroy   : function(){
            componentAdapter.beforeDestroy.apply(this,arguments);
            this.rendered && ux.Component.superclass.beforeDestroy.apply(this,arguments);
         },
        doAutoLoad : Ext.emptyFn,
        
        getContentTarget : componentAdapter.getContentTarget,
        //Ext 2.x does not have Box setAutoscroll
        setAutoScroll : componentAdapter.setAutoScroll,
        
        onResize : function(){
            ux.Component.superclass.onResize.apply(this,arguments);
            componentAdapter.onResize.apply(this,arguments);
        }
        
    });

//    Ext.reg('uxmedia', Ext.ux.Media.Component);
//    Ext.reg('media', Ext.ux.Media.Component);

    /**
     * @class Ext.ux.Media.Panel
     * @extends Ext.Panel
     * @version 2.2
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     * @constructor
     * @base Ext.ux.Media
     * @param {Object} config The config object
     */

    Ext.define("Ext.ux.Media.Panel", {
        extend: "Ext.Panel",
        alias: "mediapanel",
        cls           : "x-media-panel",

        ctype         : "Ext.ux.Media.Panel",
         
        autoScroll    : false,

          /**
           * @cfg {String} mediaEl The name of the containing element for the media.
           * @default 'body'
           */
        mediaEl       : 'body',

        mediaClass    : Ext.ux.Media,

        constructor   : function(config){
	         //Inherit the ux.Media class
	          Ext.apply(this , this.mediaClass.prototype );
	          ux.Panel.superclass.constructor.apply(this, arguments);
        },

        /** @private */
        initComponent   : function(){
            ux.Panel.superclass.initComponent.apply(this,arguments);
            componentAdapter.init.apply(this,arguments);
        },
        /** @private */
        afterRender  : function(ct){
            ux.Panel.superclass.afterRender.apply(this,arguments);
            componentAdapter.afterRender.apply(this,arguments);
         },
         /** @private */
        beforeDestroy  : function(){
            componentAdapter.beforeDestroy.apply(this,arguments);
            this.rendered && ux.Panel.superclass.beforeDestroy.apply(this,arguments);
         },
        doAutoLoad : Ext.emptyFn,
        
        getContentTarget : componentAdapter.getContentTarget,

        setAutoScroll : componentAdapter.setAutoScroll,
        
        onResize : function(){
            ux.Panel.superclass.onResize.apply(this,arguments);
            componentAdapter.onResize.apply(this,arguments);
        }

    });

    /**
     * @class Ext.ux.Media.Portlet
     * @extends Ext.ux.Media.Panel
     * @version 2.2
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     * @constructor
     * @param {Object} config The config object
     */

    Ext.define("Ext.ux.Media.Portlet", {
       extend: "Ext.ux.Media.Panel",
       alias: "mediaportlet",

       anchor       : '100%',
       frame        : true,
       collapseEl   : 'bwrap',
       collapsible  : true,
       draggable    : true,
       autoWidth    : true,
       ctype        : "Ext.ux.Media.Portlet",
       cls          : 'x-portlet x-media-portlet'

    });


   /**
     * @class Ext.ux.Media.Window
     * @extends Ext.Window
     * @version 1.0
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     * @constructor
     * @base Ext.ux.Media
     * @param {Object} config The config object
     */

    Ext.define("Ext.ux.Media.Window", {
        extend: "Ext.Window",
        alias: "mediawindow",
        /** @private */
        constructor   : function(){
          Ext.applyIf(this , this.mediaClass.prototype );
          ux.Window.superclass.constructor.apply(this, arguments);
        },

         cls           : "x-media-window",
         
         autoScroll    : false,
         
         ctype         : "Ext.ux.Media.Window",

         mediaClass    : Ext.ux.Media,

          /**
           * @cfg {String} mediaEl The name of the containing element for the media.
           * @default 'body'
           */
         mediaEl       : 'body',

        /** @private */
        initComponent   : function(){
            ux.Window.superclass.initComponent.apply(this,arguments);
            componentAdapter.init.apply(this,arguments);
        },

        /** @private */
        afterRender  : function(){
            ux.Window.superclass.afterRender.apply(this,arguments);  //wait for sizing
            componentAdapter.afterRender.apply(this,arguments);
         },
         /** @private */
        beforeDestroy   : function(){
            componentAdapter.beforeDestroy.apply(this,arguments);
            this.rendered && ux.Window.superclass.beforeDestroy.apply(this,arguments);
         },

        doAutoLoad : Ext.emptyFn,
        
        getContentTarget : componentAdapter.getContentTarget,

        setAutoScroll : componentAdapter.setAutoScroll,
        
        onResize : function(){
            ux.Window.superclass.onResize.apply(this,arguments);
            componentAdapter.onResize.apply(this,arguments);
        }

    });

    Ext.ns('Ext.capabilities');
    Ext.ns('Ext.ux.Media.plugin');
    /**
     * Check Basic HTML5 Element support for the <audio> tag and/or Audio object.
     */
    var CAPS = (Ext.capabilities.hasAudio || 
       (Ext.capabilities.hasAudio = function(){
                
                var aTag = !!document.createElement('audio').canPlayType,
                    aAudio = ('Audio' in window) ? new Audio('') : {},
                    caps = aTag || ('canPlayType' in aAudio) ? { tag : aTag, object : ('play' in aAudio)} : false,
                    mime,
                    chk,
                    mimes = {
                            mp3 : 'audio/mpeg', //mp3
                            ogg : 'audio/ogg',  //Ogg Vorbis
                            wav : 'audio/x-wav', //wav 
                            basic : 'audio/basic', //au, snd
                            aif  : 'audio/x-aiff' //aif, aifc, aiff
                        };
                    
                    if(caps && ('canPlayType' in aAudio)){
                       for (chk in mimes){ 
                            caps[chk] = (mime = aAudio.canPlayType(mimes[chk])) != 'no' && (mime != '');
                        }
                    }                     
                    return caps;
            }()));
            
     Ext.iterate || Ext.apply (Ext, {
        iterate : function(obj, fn, scope){
            if(Ext.isEmpty(obj)){
                return;
            }
            if(Ext.isIterable(obj)){
                Ext.each(obj, fn, scope);
                return;
            }else if(Ext.isObject(obj)){
                for(var prop in obj){
                    if(obj.hasOwnProperty(prop)){
                        if(fn.call(scope || obj, prop, obj[prop], obj) === false){
                            return;
                        };
                    }
                }
            }
        },
        isIterable : function(v){
            //check for array or arguments
            if(Ext.isArray(v) || v.callee){
                return true;
            }
            //check for node list type
            if(/NodeList|HTMLCollection/.test(toString.call(v))){
                return true;
            }
            //NodeList has an item and length property
            //IXMLDOMNodeList has nextNode method, needs to be checked first.
            return ((v.nextNode || v.item) && Ext.isNumber(v.length));
        },
        
        isObject : function(obj){
            return !!obj && toString.apply(obj) == '[object Object]';
        }
     });
    
     /**
     * @class Ext.ux.Media.plugin.AudioEvents
     * @extends Ext.ux.Media.Component
     * @version 1.0
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     * @constructor
     * @param {Object} config The config object
     */
     Ext.define("Ext.ux.Media.plugin.AudioEvents", {
         extend: "Ext.ux.Media.Component",
         alias: "audioevents",
         autoEl  : {tag:'div' },
       
       cls: 'x-hide-offsets',
       
       disableCaching : false,
       
       /**
        * @cfg {Object} audioEvents An object hash mapping URL of audio resources to
        * DOM Ext.Element or Ext.util.Observable events.
        * @example
        *  
        */
       audioEvents : {},
       
       /**
        * @cfg {Float} volume Desired Volume level in the range (0.0 - 1)
        * 
        */
       volume     : .5,
       
       ptype      : 'audioevents',
       
       /** @private
        * 
        */
       initComponent : function(){
          this.mediaCfg || (this.mediaCfg = {
              mediaType : 'WAV',
              start     : true,
              url       : ''
          });
          Ext.ux.Media.plugin.AudioEvents.superclass.initComponent.apply(this,arguments);
          
          this.addEvents(
          /**
            * Fires immediately preceeding an Audio Event. Returning false within this handler
            * cancels the audio playback.
            * @event beforeaudio
            * @memberOf Ext.ux.Media.plugin.AudioEvents
            * @param {Object} plugin This Media plugin instance.
            * @param {Ext.Component/Ext.Element} target The target Ext.Component or Ext.Element instance.
            * @param {String} eventName The eventName linked to the Audio stream.
           */
           'beforeaudio');
           
           this.setVolume(this.volume);
       },
       
       /** @private
        * 
        */
       init : function( target ){
        
            this.rendered || this.render(Ext.getBody());
            
            if(target.dom || target.ctype){
                var plugin = this;
                Ext.iterate(this.audioEvents || {}, 
                 function(event){
                   /* if the plugin init-target is an Observable, 
                    * assert the eventName, exiting if not defined
                    */
                    if(target.events && !target.events[event]) return;
                    
                    /**
                     * Mixin Audio Management methods to the target
                     */
                    Ext.applyIf(target, {
                       audioPlugin : plugin,
                       audioListeners : {},
                       
                       /**
                        * @method removeAudioListener 
                        * 
                        */
                       removeAudioListener : function(audioEvent){
                          if(audioEvent && this.audioListeners[audioEvent]){ 
                               this.removeListener && 
                                 this.removeListener(audioEvent, this.audioListeners[audioEvent], this);
                               delete this.audioListeners[audioEvent];
                          }
                       },
                       /**
                        * Removes all Audio Listeners from the Element or Component
                        * @method removeAudioListeners
                        */
                       removeAudioListeners : function(){
                          var c = [];
                          Ext.iterate(this.audioListeners, function(audioEvent){c.push(audioEvent)});
                          Ext.iterate(c, this.removeAudioListener, this);
                       },
                       
                       addAudioListener : function(audioEvent){
                           if(this.audioListeners[audioEvent]){
                               this.removeAudioListener(audioEvent);
                           }
                           this.addListener && 
                             this.addListener (audioEvent, 
                               this.audioListeners[audioEvent] = function(){
                               this.audioPlugin.onEvent(this, audioEvent);
                             }, this);
                        
                       } ,

                       enableAudio : function(){
                          this.audioPlugin && this.audioPlugin.enable();
                       },
                       
                       disableAudio : function(){
                          this.audioPlugin && this.audioPlugin.disable();
                       },
                       
                       setVolume : function(volume){
                          this.audioPlugin && this.audioPlugin.setVolume(volume);
                       }
                    });
                    
                    target.addAudioListener(event);
                    
                },this);
            }
       },
       
       /**
        * @param {Float} volume The volume (range 0-1)
        * @return {Object} this
        */
       setVolume   : function(volume){
            var AO = this.audioObject, v = Math.max(Math.min(parseFloat(volume)||0, 1),0);
            this.mediaCfg && (this.mediaCfg.volume = v*100);
            this.volume = v;
            AO && (AO.volume = v);
            return this;
       },
       
       /**
        * @private
        */
       onEvent : function(comp, event){
           if(!this.disabled && this.audioEvents && this.audioEvents[event]){
              if(this.fireEvent('beforeaudio',this, comp, event) !== false ){
                  this.mediaCfg.url = this.audioEvents[event];

                  if(CAPS.object){  //HTML5 Audio support?
                        this.audioObject && this.audioObject.stop && this.audioObject.stop();
                        if(this.audioObject = new Audio(this.mediaCfg.url || '')){
                            this.setVolume(this.volume);
                            this.audioObject.play && this.audioObject.play();
                        }
                  } else {
                        var O = this.getInterface();
                        if(O){ 
                            if(O.object){  //IE ActiveX
                                O= O.object;
	                            ('Open' in O) && O.Open(this.mediaCfg.url || '');
	                            ('Play' in O) && O.Play();
                            }else {  //All Others - just rerender the tag
                                this.refreshMedia();      
                            }
                            
                        }
                  }
              }
              
           }
       }
    
    });

    Ext.onReady(function(){
        //Generate CSS Rules if not defined in markup
        var CSS = Ext.util.CSS, rules=[];

        CSS.getRule('.x-media', true) || (rules.push('.x-media{width:100%;height:100%;outline:none;overflow:hidden;}'));
        CSS.getRule('.x-media-mask') || (rules.push('.x-media-mask{width:100%;height:100%;overflow:hidden;position:relative;zoom:1;}'));

        //default Rule for IMG:  h/w: auto;
        CSS.getRule('.x-media-img') || (rules.push('.x-media-img{background-color:transparent;width:auto;height:auto;position:relative;}'));

        // Add the new masking rule if not present.
        CSS.getRule('.x-masked-relative') || (rules.push('.x-masked-relative{position:relative!important;}'));

        if(!!rules.length){
             CSS.createStyleSheet(rules.join(''));
             CSS.refreshCache();
        }

    });

    /**
     * @class Ext.ux.Media.Element
     * @extends Ext.Element
     * @version 2.2
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
     * @constructor
     */
    Ext.define("Ext.ux.Media.Element", {
        extend: "Ext.Element",
        /**
        * @private
        */
        constructor   : function( element ) {
            
            Ext.ux.Media.Element.superclass.constructor.apply(this, arguments);
           
            /*
             * Ext.get does not re-assert the current Element class in the cache
             * so it must be updated manually
             */
            if(Ext.elCache){  //Ext 3.1 compat
                Ext.elCache[this.id] || (Ext.elCache[this.id] = {
                    events : {},
                    data : {}
                });
                Ext.elCache[this.id].el = this;
            }else {
                Ext.Element.cache[this.id] = this;
            }

        },

        /**
         * <object|frame|img> are not maskable by the default Element mask implementation.
         * This selects the immediate parent element as the mask target.

         * Puts a mask over the element to disable user interaction. Requires core.css.
         * @param {String} msg (optional) A message to display in the mask
         * @param {String} msgCls (optional) A css class to apply to the msg element
         * @return {Element} The mask element
         */
        mask : function(msg, msgCls){

            this.maskEl || (this.maskEl = this.parent('.x-media-mask') || this.parent());

            return this.maskEl.mask.apply(this.maskEl, arguments);

        },
        unmask : function(remove){

            if(this.maskEl){
                this.maskEl.unmask(remove);
                this.maskEl = null;
            }
        },
        
        /**
          * Removes this element from the DOM and deletes it from the cache
          * @param {Boolean} cleanse (optional) Perform a cleanse of immediate childNodes as well.
          * @param {Boolean} deep (optional) Perform a deep cleanse of all nested childNodes as well.
          */

        remove : function(cleanse, deep){
              if(this.dom){
                this.unmask(true);
                this.removeAllListeners();    //remove any Ext-defined DOM listeners
                Ext.ux.Media.Element.superclass.remove.apply(this,arguments);
                this.dom = null;  //clear ANY DOM references
              }
         }

    });

    Ext.ux.Media.prototype.elementClass  =  Ext.ux.Media.Element;

    /**
     * @class Ext.ux.IntelliMask
     * @version 1.0.1
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @constructor
     * Create a new LoadMask
     * @desc A custom utility class for generically masking elements while loading media.
     */
    Ext.ux.IntelliMask = function(el, config){

        Ext.apply(this, config || {msg : this.msg});
        this.el = Ext.get(el);

    };

    Ext.ux.IntelliMask.prototype = {

        /**
         * @cfg {Boolean} removeMask
         * True to create a single-use mask that is automatically destroyed after loading (useful for page loads),
         * False to persist the mask element reference for multiple uses (e.g., for paged/frequently masked widgets).
         * @default false.
         */

         removeMask  : false,

        /**
         * @cfg {String} msg The default text to display in a centered loading message box
         * @default 'Loading Media...'
         */
        msg : 'Loading Media...',
        /**
         * @cfg {String} msgCls
         * The CSS class to apply to the loading message element.
         * @default "x-mask-loading"
         */
        msgCls : 'x-mask-loading',


        /** @cfg {Number} zIndex The optional zIndex applied to the masking Elements
         */
        zIndex : null,

        /**
         * Read-only. True if the mask is currently disabled so that it will not be displayed (defaults to false)
         * @property {Boolean} disabled
         * @type Boolean
         */
        disabled: false,

        /**
         * Read-only. True if the mask is currently applied to the element.
         * @property {Boolean} active
         * @type Boolean
         */
        active: false,

        /**
         * @cfg {Boolean/Integer} autoHide  True or millisecond value hides the mask if the {link #hide} method is not called within the specified time limit.
         */
        autoHide: false,

        /**
         * Disables the mask to prevent it from being displayed
         */
        disable : function(){
           this.disabled = true;
        },

        /**
         * Enables the mask so that it can be displayed
         */
        enable : function(){
            this.disabled = false;
        },

        /**
         * Show this Mask over the configured Element.
         * @param {String/ConfigObject} msg The message text do display during the masking operation
         * @param {String} msgCls The CSS rule applied to the message during the masking operation.
         * @param {Function} fn The callback function to be invoked after the mask is displayed.
         * @param {Integer} fnDelay The number of milleseconds to wait before invoking the callback function
         * @return {Ext.Element} the mask container element.
         * @example
           mask.show({autoHide:3000});   //show defaults and hide after 3 seconds.
         * @example
           mask.show('Loading Content', null, loadContentFn); //show msg and execute fn
         * @example
           mask.show({
               msg: 'Loading Content',
               msgCls : 'x-media-loading',
               fn : loadContentFn,
               fnDelay : 100,
               scope : window,
               autoHide : 2000   //remove the mask after two seconds.
           });
         */
        show: function(msg, msgCls, fn, fnDelay ){

            var opt={}, autoHide = this.autoHide;
            fnDelay = parseInt(fnDelay,10) || 20; //ms delay to allow mask to quiesce if fn specified

            if(Ext.isObject(msg)){
                opt = msg;
                msg = opt.msg;
                msgCls = opt.msgCls;
                fn = opt.fn;
                autoHide = typeof opt.autoHide != 'undefined' ?  opt.autoHide : autoHide;
                fnDelay = opt.fnDelay || fnDelay ;
            }
            if(!this.active && !this.disabled && this.el){
                var mask = this.el.mask(msg || this.msg, msgCls || this.msgCls);

                this.active = !!this.el._mask;
                if(this.active){
                    if(this.zIndex){
                        this.el._mask.setStyle("z-index", this.zIndex);
                        if(this.el._maskMsg){
                            this.el._maskMsg.setStyle("z-index", this.zIndex+1);
                        }
                    }
                }
            } else {fnDelay = 0;}

            //passed function is called regardless of the mask state.
            if(typeof fn === 'function'){
                fn.defer(fnDelay ,opt.scope || null);
            } else { fnDelay = 0; }

            if(autoHide && (autoHide = parseInt(autoHide , 10)||2000)){
                this.hide.defer(autoHide+(fnDelay ||0),this );
            }

            return this.active? {mask: this.el._mask , maskMsg: this.el._maskMsg} : null;
        },

        /**
         * Hide this Mask.
         * @param {Boolean} remove  True to remove the mask element from the DOM after hide.
         */
        hide: function(remove){
            if(this.el){
                this.el.unmask(remove || this.removeMask);
            }
            this.active = false;
            return this;
        },

        // private
        destroy : function(){this.hide(true); this.el = null; }
     };



/**
 * @namespace Ext.ux.Media.mediaTypes
 */
Ext.ux.Media.mediaTypes = {

     /**
     * @namespace Ext.ux.Media.mediaTypes.WAV
     * @desc Generic WAV
     */
       
      WAV : 
            Ext.apply(
            { tag      : 'object'
             ,cls      : 'x-media x-media-wav'
             ,data      : "@url"
             ,type     : 'audio/x-wav'
             ,loop  : false
             ,params  : {

                  filename     : "@url"
                 ,displaysize  : 0
                 ,autostart    : '@start'
                 ,showControls : '@controls'
                 ,showStatusBar:  false
                 ,showaudiocontrols : '@controls'
                 ,stretchToFit  : false
                 ,Volume        : "@volume"
                 ,PlayCount     : 1

               }
           },Ext.isIE?{
               classid :"CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95" //default for WMP installed w/Windows
               ,codebase:"http" + ((Ext.isSecure) ? 's' : '') + "://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701"
               ,type:'application/x-oleobject'
               }:
               {src:"@url"}),
    
        /**
         * @namespace Ext.ux.Media.mediaTypes.PDF
         *  OLE-TLB public IAcroAXDocShim interface (available on IE only)
         *  
    function LoadFile(const fileName: WideString): WordBool;
    procedure setShowToolbar(On_: WordBool);
    procedure gotoFirstPage;
    procedure gotoLastPage;
    procedure gotoNextPage;
    procedure gotoPreviousPage;
    procedure setCurrentPage(n: Integer);
    procedure goForwardStack;
    procedure goBackwardStack;
    procedure setPageMode(const pageMode: WideString);
    procedure setLayoutMode(const layoutMode: WideString);
    procedure setNamedDest(const namedDest: WideString);
    procedure printAll;
    procedure Print;
    procedure printWithDialog;
    procedure setZoom(percent: Single);
    procedure setZoomScroll(percent: Single; left: Single; top: Single);
    procedure setView(const viewMode: WideString);
    procedure setViewScroll(const viewMode: WideString; offset: Single);
    procedure AboutBox;
    procedure printPages(from: Integer; to_: Integer);
    procedure printPagesFit(from: Integer; to_: Integer; shrinkToFit: WordBool);
    procedure setViewRect(left: Single; top: Single; width: Single; height: Single);
    procedure printAllFit(shrinkToFit: WordBool);
    procedure setShowScrollbars(On_: WordBool);
    property  ControlInterface: _DPdf read GetControlInterface;
    property  DefaultInterface: _DPdf read GetControlInterface;
  published
    property Anchors;
    property  TabStop;
    property  Align;
    property  DragCursor;
    property  DragMode;
    property  ParentShowHint;
    property  PopupMenu;
    property  ShowHint;
    property  TabOrder;
    property  Visible;
    property  OnDragDrop;
    property  OnDragOver;
    property  OnEndDrag;
    property  OnEnter;
    property  OnExit;
    property  OnStartDrag;
    property src: WideString index 1 read GetWideStringProp write SetWideStringProp stored False;
  end;
         */

       PDF : Ext.apply({  //Acrobat plugin thru release 8.0 all crash FF3
                tag     : 'object'
               ,cls     : 'x-media x-media-pdf'
               ,type    : "application/pdf"
               ,data    : "@url"
               ,autoSize:true
               ,params  : { src : "@url"}
               },Ext.isIE?{
                   classid :"CLSID:CA8A9780-280D-11CF-A24D-444553540000"
                   }:false),


      /**
       * @namespace Ext.ux.Media.mediaTypes.PDFFRAME
       * @desc Most reliable method for Acrobat on all browsers!!
       */
      PDFFRAME  : {
                  tag      : 'iframe'
                 ,cls      : 'x-media x-media-pdf-frame'
                 ,frameBorder : 0
                 ,style    : { 'z-index' : 2}
                 ,src      : "@url"
                 ,autoSize :true
        },

       /**
         * @namespace Ext.ux.Media.mediaTypes.WMV
         * @desc <pre><code>WMV Interface Notes
           On the original player (pre XP) IE only, to retrieve the object interface (for controlling player via JS)
            use mediaComp.getInterface().object.controls

           Other browsers do NOT support a Javascript interface

           Related DOM attributes for WMV:
                 DataFormatAs :
                 Name :
                 URL :
                 OpenState:6
                 PlayState:0
                 Controls :
                 Settings :
                 CurrentMedia:null
                 MediaCollection :
                 PlaylistCollection :
                 VersionInfo:9.0.0.3008
                 Network :
                 CurrentPlaylist :
                 CdromCollection :
                 ClosedCaption :
                 IsOnline:false
                 Error :
                 Status :
                 Dvd :
                 Enabled:true
                 FullScreen:false
                 EnableContextMenu:true
                 UiMode:full
                 StretchToFit:false
                 WindowlessVideo:false
                 IsRemote:false</code></pre>
        */

      WMV : Ext.apply(
              {tag      :'object'
              ,cls      : 'x-media x-media-wmv'
              ,type     : 'application/x-mplayer2'
              //,type   : "video/x-ms-wmv"
              ,data     : "@url"
              ,autoSize : true
              ,params  : {

                  filename     : "@url"
                 ,displaysize  : 0
                 ,autostart    : "@start"
                 ,showControls : "@controls"
                 ,showStatusBar: "@status"
                 ,showaudiocontrols : true
                 ,stretchToFit  : true
                 ,Volume        :"@volume"
                 ,PlayCount     : 1

               }
               },Ext.isIE?{
                   classid :"CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95" //default for WMP installed w/Windows
                   ,codebase:"http" + ((Ext.isSecure) ? 's' : '') + "://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701"
                   ,type:'application/x-oleobject'
                   }:
               {src:"@url"}
             ),
       /**
       * @namespace Ext.ux.Media.mediaTypes.APPLET
       */            
       APPLET  : {
                  tag      :'object'
                 ,cls      : 'x-media x-media-applet'
                 ,type     : 'application/x-java-applet'
                 ,unsupportedText : {tag : 'p', html:'Java is not installed/enabled.'}
                 ,params : {
                   url : '@url',
                   archive : '',  //the jar file
                   code    : '' //the Java class
                  }
       },
       
       "AUDIO-OGG"   : {
           tag      : 'audio',
           controls : '@controls',
           src      : '@url'
       },
       
       "VIDEO-OGG"   : {
           tag      : 'video',
           controls : '@controls',
           src      : '@url'
       },

     /**
       * @namespace Ext.ux.Media.mediaTypes.SWF
       */
       SWF   :  Ext.apply({
                  tag      :'object'
                 ,cls      : 'x-media x-media-swf'
                 ,type     : 'application/x-shockwave-flash'
                 ,scripting: 'sameDomain'
                 ,standby  : 'Loading..'
                 ,loop     :  true
                 ,start    :  false
                 ,unsupportedText : {cn:['The Adobe Flash Player is required.',{tag:'br'},{tag:'a',cn:[{tag:'img',src:'http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif'}],href:'http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash',target:'_flash'}]}
                 ,params   : {
                      movie     : "@url"
                     ,menu      : "@controls"
                     ,play      : "@start"
                     ,quality   : "high"
                     ,allowscriptaccess : "@scripting"
                     ,allownetworking : 'all'
                     ,allowfullScreen : false
                     ,bgcolor   : "#FFFFFF"
                     ,wmode     : "opaque"
                     ,loop      : "@loop"
                    }

                },Ext.isIE?
                    {classid :"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
                     codebase:"http" + ((Ext.isSecure) ? 's' : '') + "://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,0,0"
                    }:
                    {data     : "@url"}),
      /**
       * @namespace Ext.ux.Media.mediaTypes.SCRIBD
       * sample url : http://documents.scribd.com/ScribdViewer.swf?document_id=502727&access_key=cwy7bk66jc0l&page=1&version=1&viewMode=
       */
       SCRIBD :  Ext.apply({
                  tag      :'object'
                 ,cls      : 'x-media x-media-scribd'
                 ,type     : 'application/x-shockwave-flash'
                 ,scripting: 'always'
                 ,standby  : 'Loading..'
                 ,loop     :  true
                 ,start    :  false
                 ,unsupportedText : {cn:['The Adobe Flash Player is required.',{tag:'br'},{tag:'a',cn:[{tag:'img',src:'http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif'}],href:'http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash',target:'_flash'}]}
                 ,params   : {
                      movie     : "@url"
                     ,menu      : "@controls"
                     ,play      : "@start"
                     ,quality   : "high"
                     ,menu      : true
                     ,scale     : 'showall'
                     ,salign    : ' '
                     ,allowscriptaccess : "@scripting"
                     ,allownetworking : 'all'
                     ,allowfullScreen : true
                     ,bgcolor   : "#FFFFFF"
                     ,wmode     : "opaque"
                     ,loop      : "@loop"
                    }

                },Ext.isIE?
                    {classid :"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
                     codebase:"http" + ((Ext.isSecure) ? 's' : '') + "://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,0,0"
                    }:
                    {data     : "@url"}),
                    
      /**
       * @namespace Ext.ux.Media.mediaTypes.JWP
       * @see http://code.jeroenwijering.com/trac/wiki/FlashAPI
       */

        JWP :  Ext.apply({
              tag      :'object'
             ,cls      : 'x-media x-media-swf x-media-flv'
             ,type     : 'application/x-shockwave-flash'
             ,data     : "@url"
             ,loop     :  false
             ,start    :  false
             //ExternalInterface bindings
             ,boundExternals : ['sendEvent' , 'addModelListener', 'addControllerListener', 'addViewListener', 'getConfig', 'getPlaylist']
             ,params   : {
                 movie     : "@url"
                ,flashVars : {
                               autostart:'@start'
                              ,repeat   :'@loop'
                              ,height   :'@height'
                              ,width    :'@width'
                              ,id       :'@id'
                              }
                }

        },Ext.isIE?{
             classid :"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
            ,codebase:"http" + ((Ext.isSecure) ? 's' : '') + "://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0"
            }:false),


        /**
         * @namespace Ext.ux.Media.mediaTypes.QT
         * @desc QT references: http://developer.apple.com/mac/library/documentation/QuickTime/Conceptual/QTScripting_HTML/QTScripting_HTML_Document/ScriptingHTML.html
         */
        QT : Ext.apply({
                       tag      : 'object'
                      ,cls      : 'x-media x-media-quicktime'
                      ,type     : "video/quicktime"
                      ,style    : {position:'relative',"z-index":1 ,behavior:'url(#qt_event_source)'}
                      ,scale    : 'aspect'  // ( .5, 1, 2 , ToFit, Aspect )
                      ,unsupportedText : '<a href="http://www.apple.com/quicktime/download/">Get QuickTime</a>'
                      ,scripting : true
                      ,volume   : '50%'   //also 0-255
                      ,data     : '@url'
                      ,params   : {
                           src          : Ext.isIE?'@url': null
                          ,href        : "http://quicktime.com"
                          ,target      : "_blank"
                          ,autoplay     : "@start"
                          ,targetcache  : true
                          ,cache        : true
                          ,wmode        : 'opaque'
                          ,controller   : "@controls"
                      ,enablejavascript : "@scripting"
                          ,loop         : '@loop'
                          ,scale        : '@scale'
                          ,volume       : '@volume'
                          ,QTSRC        : '@url'

                       }

                     },Ext.isIE?
                          { classid      :'clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B'
                           ,codebase     :"http" + ((Ext.isSecure) ? 's' : '') + '://www.apple.com/qtactivex/qtplugin.cab#version=7,2,1,0'

                       }:
                       {
                         PLUGINSPAGE  : "http://www.apple.com/quicktime/download/"

                    }),

        /**
         * @namespace Ext.ux.Media.mediaTypes.QTEVENTS
         * @desc For QuickTime DOM event support include this <object> tag structure in the <head> section
         */


        QTEVENTS : {
                   tag      : 'object'
                  ,id       : 'qt_event_source'
                  ,cls      : 'x-media x-media-qtevents'
                  ,type     : "video/quicktime"
                  ,params   : {}
                  ,classid      :'clsid:CB927D12-4FF7-4a9e-A169-56E4B8A75598'
                  ,codebase     :"http" + ((Ext.isSecure) ? 's' : '') + '://www.apple.com/qtactivex/qtplugin.cab#version=7,2,1,0'
                 },

        /**
         * @namespace Ext.ux.Media.mediaTypes.WPMP3
         * @desc WordPress Audio Player : http://wpaudioplayer.com/
         */

        WPMP3 : Ext.apply({
                       tag      : 'object'
                      ,cls      : 'x-media x-media-audio x-media-wordpress'
                      ,type     : 'application/x-shockwave-flash'
                      ,data     : '@url'
                      ,start    : true
                      ,loop     : false
                      ,boundExternals : ['open','close','setVolume','load']
                      ,params   : {
                           movie        : "@url"
                          ,width        :'@width'  //required
                          ,flashVars : {
                               autostart    : "@start"
                              ,controller   : "@controls"
                              ,enablejavascript : "@scripting"
                              ,loop         :'@loop'
                              ,scale        :'@scale'
                              ,initialvolume:'@volume'
                              ,width        :'@width'  //required
                              ,encode       : 'no'  //mp3 urls will be encoded
                              ,soundFile    : ''   //required
                          }
                       }
                    },Ext.isIE?{classid :"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"}:false),

        /**
         * @namespace Ext.ux.Media.mediaTypes.REAL
         * @desc Real Player
         * Parameter Reference for Real Player: http://service.real.com/help/library/guides/extend/htmfiles/appc_par.htm
         */

        REAL : Ext.apply({
                tag     :'object'
               ,cls     : 'x-media x-media-real'
               ,type    : "audio/x-pn-realaudio-plugin"
               ,data    : "@url"
               ,controls: 'all'
               ,start   : -1
               ,standby : "Loading Real Media Player components..."
               ,params   : {
                          src        : "@url"
                         ,autostart  : "@start"
                         ,center     : false
                         ,maintainaspect : true
                         ,prefetch   : false
                         ,controller : "@controls"
                         ,controls   : "@controls"
                         ,volume     :'@volume'
                         ,loop       : "@loop"
                         ,numloop    : null
                         ,shuffle    : false
                         ,console    : "_master"
                         ,backgroundcolor : '#000000'
                         }

                },Ext.isIE?{classid :"clsid:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA"}:false),

        /**
         * @namespace Ext.ux.Media.mediaTypes.SVG
         * @desc Generic SVG
         */

        SVG : {
                  tag      : 'object'
                 ,cls      : 'x-media x-media-img x-media-svg'
                 ,type     : "image/svg+xml"
                 ,data     : "@url"
                 ,params   : { src : "@url"}

        },

        /**
         * @namespace Ext.ux.Media.mediaTypes.GIF
         * @desc
         */

        GIF : {
                  tag      : 'img'
                 ,cls      : 'x-media x-media-img x-media-gif'
                 ,src     : "@url"
        },

        /**
         * @namespace Ext.ux.Media.mediaTypes.TIFF
         * @desc
         */

        TIFF : {
                  tag      : 'object'
                 ,type     : "image/tiff"
                 ,cls      : 'x-media x-media-img x-media-tiff'
                 ,data     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.JPEG
         */

        JPEG : {
                  tag      : 'img'
                 ,cls      : 'x-media x-media-img x-media-jpeg'
                 //,style    : {overflow:'hidden', display:'inline'}
                 ,src     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.JP2
         */

        JP2 :{
                  tag      : 'object'
                 ,cls      : 'x-media x-media-img x-media-jp2'
                 ,type     : Ext.isIE ? "image/jpeg2000-image" : "image/jp2"
                 ,data     : "@url"
                },
        /**
         * @namespace Ext.ux.Media.mediaTypes.PNG
         */
        PNG : {
                  tag      : 'img'
                 ,cls      : 'x-media x-media-img x-media-png'
                 ,src     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.HTM
         */

        HTM : {
                  tag      : 'iframe'
                 ,cls      : 'x-media x-media-html'
                 ,frameBorder : 0
                 ,autoSize : true
                 ,style    : {overflow:'auto', 'z-index' : 2}
                 ,src     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.TXT
         */

        TXT : {
                  tag      : 'object'
                 ,cls      : 'x-media x-media-text'
                 ,type     : "text/plain"
                 ,style    : {overflow:'auto',width:'100%',height:'100%'}
                 ,data     : "@url"
        },

        /**
         * @namespace Ext.ux.Media.mediaTypes.RTF
         */

        RTF : {
                  tag      : 'object'
                 ,cls      : 'x-media x-media-rtf'
                 ,type     : "application/rtf"
                 ,style    : {overflow:'auto',width:'100%',height:'100%'}
                 ,data     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.JS
         */

        JS : {
                  tag      : 'object'
                 ,cls      : 'x-media x-media-js'
                 ,type     : "text/javascript"
                 ,style    : {overflow:'auto',width:'100%',height:'100%'}
                 ,data     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.CSS
         */

        CSS : {
                  tag      : 'object'
                 ,cls      : 'x-media x-media-css'
                 ,type     : "text/css"
                 ,style    : {overflow:'auto',width:'100%',height:'100%'}
                 ,data     : "@url"
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.SILVERLIGHT
         */

        SILVERLIGHT : {
              tag      : 'object'
             ,cls      : 'x-media x-media-silverlight'
             ,type      :"application/ag-plugin"
             ,data     : "@url"
             ,params  : { MinRuntimeVersion: "1.0" , source : "@url" }
        },
        /**
         * @namespace Ext.ux.Media.mediaTypes.SILVERLIGHT2
         */

        SILVERLIGHT2 : {
              tag      : 'object'
             ,cls      : 'x-media x-media-silverlight'
             ,type      :"application/x-silverlight-2"
             ,data     : "data:application/x-silverlight,"
             ,params  : { MinRuntimeVersion: "2.0" }
             ,unsupportedText: '<a href="http://go2.microsoft.com/fwlink/?LinkID=114576&v=2.0"><img style="border-width: 0pt;" alt="Get Microsoft Silverlight" src="http://go2.microsoft.com/fwlink/?LinkID=108181"/></a>'
        },
 
        /**
         * @namespace Ext.ux.Media.mediaTypes.XML
         */

        XML : {
              tag      : 'iframe'
             ,cls      : 'x-media x-media-xml'
             ,style    : {overflow:'auto'}
             ,src     : "@url"
        },

        /**
         * @namespace Ext.ux.Media.mediaTypes.VLC
         */

        //VLC ActiveX Player -- Suffers the same fate as the Acrobat ActiveX Plugin
        VLC : Ext.apply({
              tag      : 'object'
             ,cls      : 'x-media x-media-vlc'
             ,type     : "application/x-google-vlc-plugin"
             ,pluginspage:"http://www.videolan.org"
             ,events   : true
             ,start    : false
             ,params   : {
                   Src        : "@url"
                  ,MRL        : "@url"
                  ,autoplay  :  "@start"
                  ,ShowDisplay: "@controls"
                  ,Volume     : '@volume'
                  ,Autoloop   : "@loop"

                }

             },Ext.isIE?{
                  classid     :"clsid:9BE31822-FDAD-461B-AD51-BE1D1C159921"
                 ,CODEBASE    :"http" + ((Ext.isSecure) ? 's' : '') + "://downloads.videolan.org/pub/videolan/vlc/latest/win32/axvlc.cab"
             }:{target : '@url'}),
             
        /**
         * @namespace Ext.ux.Media.mediaTypes.ODT
         * Open Office 2.0+ ODT  Text Document
         * <SCRIPT TYPE="text/javascript">

function DoThePrint() {
  SvcMgr =new ActiveXObject('com.sun.star.ServiceManager');
  Desktop = SvcMgr.CreateInstance('com.sun.star.frame.Desktop');
  oEnum = Desktop.getComponents().createEnumeration();
  for (;oEnum.hasMoreElements()==true;) {
    oo = oEnum.nextElement()
    if (oo.supportsService("com.sun.star.text.TextDocument")==true) ooDoc = oo;
  }
  alert(ooDoc.getUrl()); //if you embed many OO text objects, test here for the src in the OBJECT ...

  oFrame     = ooDoc.CurrentController.Frame
  dispatcher = SvcMgr.CreateInstance("com.sun.star.frame.DispatchHelper")
  dispatcher.executeDispatch(oFrame, ".uno:print", "", 0, Array()) 
}

 
</SCRIPT>
Simple Print
 ooDoc.print( Array() );
         * 
         */
        
        ODT : Ext.apply({
              tag      : 'object'
             ,cls      : 'x-media x-media-odt'
             ,type     : "application/vnd.oasis.opendocument.text"
             ,data     : "@url"
             ,params   : {
                   src        : '@url'
                } 
             },Ext.isIE?{
                  classid     :"clsid:67F2A879-82D5-4A6D-8CC5-FFB3C114B69D"
             }:false),
        
        /**
         * @namespace Ext.ux.Media.mediaTypes.ODS
         * Open Office 2.0+ ODS  Spreadsheet
         */
             
        ODS : Ext.apply({
              tag      : 'object'
             ,cls      : 'x-media x-media-odt'
             ,type     : "application/vnd.oasis.opendocument.spreadsheet"
             ,data     : "@url"
             ,params   : {
                   src        : '@url' 
                }
             },Ext.isIE?{
                  classid     :"clsid:67F2A879-82D5-4A6D-8CC5-FFB3C114B69D"
             }:false),
             
         /**
         * @namespace Ext.ux.Media.mediaTypes.ODS
         * Open Office 2.0+ ODS  Spreadsheet
         */
             
        IMPRESS : Ext.apply({
              tag      : 'object'
             ,cls      : 'x-media x-media-sxi'
             ,start    : false
             ,type     : "application/vnd.sun.xml.impress"
             ,data     : "@url"
             ,params   : {
                   wmode      : 'transparent',
                   src        : Ext.isIE ? '@url' : null
                }
             },Ext.isIE?{
                  classid     :"clsid:67F2A879-82D5-4A6D-8CC5-FFB3C114B69D"
                 
             }:{
               data     : "@url"
              
             }) 
 
    };

if (Ext.provide) {
    Ext.provide('uxmedia');
}

Ext.applyIf(Array.prototype, {

    /*
     * Fix for IE/Opera, which does not seem to include the map
     * function on Array's
     */
    map : function(fun, scope) {
        var len = this.length;
        if (typeof fun != "function") {
            throw new TypeError();
        }
        var res = new Array(len);

        for (var i = 0; i < len; i++) {
            if (i in this) {
                res[i] = fun.call(scope || this, this[i], i, this);
            }
        }
        return res;
    }
});

/*
 * My.Window = Ext.extendX(Ext.Window, function(supr){
    return {
        initComponent : function(){
            this.foo = 1;
            supr.initComponent.call(this);
        }
    }                
});
 */

/* Previous Release compatability: */

Ext.ux.MediaComponent = Ext.ux.Media.Component;
Ext.ux.MediaPanel     = Ext.ux.Media.Panel;
Ext.ux.MediaPortlet   = Ext.ux.Media.Portlet;
Ext.ux.MediaWindow    = Ext.ux.Media.Window;

})();
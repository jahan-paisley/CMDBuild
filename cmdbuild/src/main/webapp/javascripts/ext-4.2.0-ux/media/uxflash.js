/* global Ext */
/*
 * version  2.1
 * author Doug Hendricks. doug[always-At]theactivegroup.com
 * Copyright 2007-2009, Active Group, Inc.  All rights reserved.
 *
 ************************************************************************************
 *   This file is distributed on an AS IS BASIS WITHOUT ANY WARRANTY;
 *   without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 ************************************************************************************

 License: ux.Media.Flash classes are licensed under the terms of
 the Open Source GPL 3.0 license (details: http://www.gnu.org/licenses/gpl.html).

 Commercial use is prohibited without a Commercial License. See http://licensing.theactivegroup.com.

 Donations are welcomed: http://donate.theactivegroup.com

 Notes: the <embed> tag is NOT used(or necessary) in this implementation

 Version:
        2.1
           Addresses the Flash visibility/re-initialization issues for all browsers.
           Adds bi-directional fscommand support for all A-Grade browsers.
           Adds ExternalInterface Binding support with optional externalsNamespace support


        Rc1
           Adds inline media rendering within markup: <div><script>document.write(String(new Ext.ux.Media.Flash(mediaCfg)));</script></div>
           New extensible classes :
              ux.Media.Flash
              ux.FlashComponent
              ux.FlashPanel
              ux.FlashWindow

   A custom implementation for advanced Flash object interaction
       Supports:
            version detection,
            version assertion,
            Flash Express Installation (inplace version upgrades),
            and custom Event Sync for interaction with SWF.ActionScript.

    mediaCfg: {Object}
         {
           url       : Url resource to load when rendered
          ,loop      : (true/false)
          ,start     : (true/false)
          ,height    : (defaults 100%)
          ,width     : (defaults 100%)
          ,scripting : (true/false) (@macro enabled)
          ,controls  : optional: show plugins control menu (true/false)
          ,eventSynch: (Bool) If true, this class initializes an internal event Handler for
                       ActionScript event synchronization
          ,listeners  : {"mouseover": function() {}, .... } DOM listeners to set on the media object each time the Media is rendered.
          ,requiredVersion: (String,Array,Number) If specified, used in version detection.
          ,unsupportedText: (String,DomHelper cfg) Text to render if plugin is not installed/available.
          ,installUrl:(string) Url to inline SWFInstaller, if specified activates inline Express Install.
          ,installRedirect : (string) optional post install redirect
          ,installDescriptor: (Object) optional Install descriptor config
         }
    */

(function(){

   var ux = Ext.ux.Media;
    /**
     *
     * @class Ext.ux.Media.Flash
     * @extends Ext.ux.Media
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @constructor
     * @version 2.1
     * @desc
     * Base Media Class for Flash objects. Used for rendering Flash Objects for use with inline markup.

    */

    Ext.ux.Media.Flash = Ext.extend( Ext.ux.Media, {

        varsName       :'flashVars',

       /**
        *
        * @cfg {String} externalsNamespace  Defines the namespace within the Flash class instance to hold
          references to Flash ExternalInterface methods.  <p>If null, the namespace defaults to class instance (this) itself.
          If specified, the namespace is created when ExternalInterface bindings are created.<p>
          with:   externalsNamespace : 'player', you would reference ExternalInterface methods as:
          @example
             this.player.Play();
        */
        externalsNamespace :  null,

        /** @private */
        mediaType: Ext.apply({
              tag      : 'object'
             ,cls      : 'x-media x-media-swf'
             ,type     : 'application/x-shockwave-flash'
             ,loop     : null
             ,style   : {'z-index':0}
             ,scripting: "sameDomain"
             ,start    : true
             ,unsupportedText : {cn:['The Adobe Flash Player{0}is required.',{tag:'br'},{tag:'a',cn:[{tag:'img',src:'http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif'}],href:'http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash',target:'_flash'}]}
             ,params   : {
                  movie     : "@url"
                 ,play      : "@start"
                 ,loop      : "@loop"
                 ,menu      : "@controls"
                 ,quality   : "high"
                 ,bgcolor   : "#FFFFFF"
                 ,wmode     : "opaque"
                 ,allowscriptaccess : "@scripting"
                 ,allowfullscreen : false
                 ,allownetworking : 'all'
                }
             },Ext.isIE?
                    {classid :"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
                     codebase:"http" + ((Ext.isSecure) ? 's' : '') + "://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0"
                    }:
                    {data     : "@url"}),

        /** @private */
        getMediaType: function(){
             return this.mediaType;
        },

        /** @private
         * Remove the following characters to permit Flash ExternalInterface: +-/\*.
         */
        assertId : function(id, def){
             id || (id = def || Ext.id());
             return id.replace(/\+|-|\\|\/|\*/g,'');
         },

        /** @private
         *  (called once by the constructor)
         */
        initMedia : function(){

            ux.Flash.superclass.initMedia.call(this);

            var mc = Ext.apply({}, this.mediaCfg||{});
            var requiredVersion = (this.requiredVersion = mc.requiredVersion || this.requiredVersion|| false ) ;
            var hasFlash  = !!(this.playerVersion = this.detectFlashVersion());
            var hasRequired = hasFlash && (requiredVersion?this.assertVersion(requiredVersion):true);

            var unsupportedText = this.assert(mc.unsupportedText || this.unsupportedText || (this.getMediaType()||{}).unsupportedText,null);
            if(unsupportedText){
                 unsupportedText = Ext.DomHelper.markup(unsupportedText);
                 unsupportedText = mc.unsupportedText = String.format(unsupportedText,
                     (requiredVersion?' '+requiredVersion+' ':' '),
                     (this.playerVersion?' '+this.playerVersion+' ':' Not installed.'));
            }
            mc.mediaType = "SWF";

            if(!hasRequired ){
                this.autoMask = false;

                //Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
                var canInstall = hasFlash && this.assertVersion('6.0.65');
                if(canInstall && mc.installUrl){

                       mc =  mc.installDescriptor || {
                           mediaType  : 'SWF'
                            ,tag      : 'object'
                            ,cls      : 'x-media x-media-swf x-media-swfinstaller'
                            ,id       : 'SWFInstaller'
                            ,type     : 'application/x-shockwave-flash'
                            ,data     : "@url"
                            ,url              : this.prepareURL(mc.installUrl)
                            //The dimensions of playerProductInstall.swf must be at least 310 x 138 pixels,
                            ,width            : (/%$/.test(mc.width)) ? mc.width : ((parseInt(mc.width,10) || 0) < 310 ? 310 :mc.width)
                            ,height           : (/%$/.test(mc.height))? mc.height :((parseInt(mc.height,10) || 0) < 138 ? 138 :mc.height)
                            ,loop             : false
                            ,start            : true
                            ,unsupportedText  : unsupportedText
                            ,params:{
                                      quality          : "high"
                                     ,movie            : '@url'
                                     ,allowscriptacess : "always"
                                     ,wmode            : "opaque"
                                     ,align            : "middle"
                                     ,bgcolor          : "#3A6EA5"
                                     ,pluginspage      : mc.pluginsPage || this.pluginsPage || "http://www.adobe.com/go/getflashplayer"
                                   }
                        };
                        mc.params[this.varsName] = "MMredirectURL="+( mc.installRedirect || window.location)+
                                            "&MMplayerType="+(Ext.isIE?"ActiveX":"Plugin")+
                                            "&MMdoctitle="+(document.title = document.title.slice(0, 47) + " - Flash Player Installation");
                } else {
                    //Let superclass handle with unsupportedText property
                    mc.mediaType=null;
                }
            }

            /**
            *  Sets up a eventSynch between the ActionScript environment
            *  and converts it's events into native Ext events.
            *  When this config option is true, binds an ExternalInterface definition
            *  to the ux.Media.Flash class method Ext.ux.Media.Flash.eventSynch.
            *
            *  The default binding definition pass the following flashVars to the Flash object:
            *
            *  allowedDomain,
            *  elementID (the ID assigned to the DOM <object> )
            *  eventHandler (the globally accessible function name of the handler )
            *     the default implementation expects a call signature in the form:
            *
            *    ExternalInterface.call( 'eventHandler', elementID, eventString )

            *  For additional flexibility, your own eventSynch may be defined to match an existing
            *  ActionScript ExternalInterface definition.
            */

            if(mc.eventSynch){
                mc.params || (mc.params = {});
                var vars = mc.params[this.varsName] || (mc.params[this.varsName] = {});
                if(typeof vars === 'string'){ vars = Ext.urlDecode(vars,true); }
                var eventVars = (mc.eventSynch === true ? {
                         allowedDomain  : vars.allowedDomain || document.location.hostname
                        ,elementID      : mc.id || (mc.id = Ext.id())
                        ,eventHandler   : 'Ext.ux.Media.Flash.eventSynch'
                        }: mc.eventSynch );

                Ext.apply(mc.params,{
                     allowscriptaccess  : 'always'
                })[this.varsName] = Ext.applyIf(vars,eventVars);
            }

            this.bindExternals(mc.boundExternals);

            delete mc.requiredVersion;
            delete mc.installUrl;
            delete mc.installRedirect;
            delete mc.installDescriptor;
            delete mc.eventSynch;
            delete mc.boundExternals;

            this.mediaCfg = mc;


        },


        /**
        * Asserts the desired version against the installed Flash Object version.
        * @param {mixed} versionMap Acceptable parameter formats for versionMap:
        *
        *  '9.0.40' (string)<br>
        *   9  or 9.1  (number)<br>
        *   [9,0,43]  (array)
        *
        * @return {Boolean} true if the desired version is => installed version
        *  and false for all other conditions
        */
        assertVersion : function(versionMap){

            var compare;
            versionMap || (versionMap = []);

            if(Ext.isArray(versionMap)){
                compare = versionMap;
            } else {
                compare = String(versionMap).split('.');
            }
            compare = (compare.concat([0,0,0,0])).slice(0,3); //normalize

            var tpv;
            if(!(tpv = this.playerVersion || (this.playerVersion = this.detectFlashVersion()) )){ return false; }

            if (tpv.major > parseFloat(compare[0])) {
                        return true;
            } else if (tpv.major == parseFloat(compare[0])) {
                   if (tpv.minor > parseFloat(compare[1]))
                            {return true;}
                   else if (tpv.minor == parseFloat(compare[1])) {
                        if (tpv.rev >= parseFloat(compare[2])) { return true;}
                        }
                   }
            return false;
        },

       /**
        * Flash version detection function
        * @returns {Object} {major,minor,rev} version object or
        * false if Flash is not installed or detection failed.
        */
        detectFlashVersion : function(){
            if(ux.Flash.prototype.flashVersion ){
                return this.playerVersion = ux.Flash.prototype.flashVersion;
            }
            var version=false;
            var formatVersion = function(version){
              return version && !!version.length?
                {major:version[0] !== null? parseInt(version[0],10): 0
                ,minor:version[1] !== null? parseInt(version[1],10): 0
                ,rev  :version[2] !== null? parseInt(version[2],10): 0
                ,toString : function(){return this.major+'.'+this.minor+'.'+this.rev;}
                }:false;
            };
            var sfo= null;
            if(Ext.isIE){

                try{
                    sfo = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");
                }catch(e){
                    try {
                        sfo = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6");
                        version = [6,0,21];
                        // error if player version < 6.0.47 (thanks to Michael Williams @ Adobe for this solution)
                        sfo.allowscriptaccess = "always";
                    } catch(ex) {
                        if(version && version[0] === 6)
                            {return formatVersion(version); }
                        }
                    try {
                        sfo = new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
                    } catch(ex1) {}
                }
                if (sfo) {
                    version = sfo.GetVariable("$version").split(" ")[1].split(",");
                }
             }else if(navigator.plugins && navigator.mimeTypes.length){
                sfo = navigator.plugins["Shockwave Flash"];
                if(sfo && sfo.description) {
                    version = sfo.description.replace(/([a-zA-Z]|\s)+/, "").replace(/(\s+r|\s+b[0-9]+)/, ".").split(".");
                }
            }
            return (this.playerVersion = ux.Flash.prototype.flashVersion = formatVersion(version));

        }

        /** @private */
        ,onAfterMedia : function(ct){

              ux.Flash.superclass.onAfterMedia.apply(this,arguments);
              var mo;
              if(mo = this.mediaObject){

                  var id = mo.id;
                  if(Ext.isIE ){

                    //fscommand bindings
                    //implement a fsCommand event interface since its not supported on IE when writing innerHTML

                    if(!(Ext.query('script[for='+id+']').length)){
                      writeScript('var c;if(c=Ext.getCmp("'+this.id+'")){c.onfsCommand.apply(c,arguments);}',
                                  {event:"FSCommand", htmlFor:id});
                    }
                  }else{
                      window[id+'_DoFSCommand'] || (window[id+'_DoFSCommand']= this.onfsCommand.createDelegate(this));
                  }
              }
         },

        /** Remove (safely) an existing mediaObject from the Component.
         *
         */
        clearMedia  : function(){

           //de-register fscommand hooks
           if(this.mediaObject){
               var id = this.mediaObject.id;
               if(Ext.isIE){
                    Ext.select('script[for='+id+']',true).remove();
               } else {
                    window[id+'_DoFSCommand']= null;
                    delete window[id+'_DoFSCommand'];
               }
           }

           return ux.Flash.superclass.clearMedia.call(this) || this;

        },

        /**
         * Returns a reference to the embedded Flash object
         * @return {HTMLElement}
         */
        getSWFObject : function() {
            return this.getInterface();
        },


        /**
         * @private
         * fscommand handler
         * ref: http://www.northcode.com/blog.php/2007/09/11/FSCommand-and-getURL-Bug-in-Flash-Player-9
         */

        onfsCommand : function( command, args){

            if(this.events){
                this.fireEvent('fscommand', this, command ,args );
            }

        },

        /**
         * Use Flash's SetVariable method if available
         * @param {String} varName The named variable to set.
         * @param {Mixed} value The value to set.
         * @return {Boolean} Returns True if the operation was successful.
         *
         */

        setVariable : function(varName, value){
            var fo = this.getInterface();
            if(fo && 'SetVariable' in fo){
                fo.SetVariable(varName,value);
                return true;
            }
            fo = null;
            return false;

        },

       /** Use Flash's GetVariable method if available
        * @param {string} varName The named variable to retrieve.
        * @return {Mixed} returns 'undefined' if the function is not supported.
        */
        getVariable : function(varName ){
            var fo = this.getInterface();
            if(fo && 'GetVariable' in fo){
                return fo.GetVariable(varName );
            }
            fo = null;
            return undefined;

        },

        /** Helper method used to bind Flash ExternalInterface methods to a property on the ux.Flash Component level.<p>
         * Note: ExternalInterface bindings are maintained on the{@link #Ext.ux.Media.Flash-externalsNamespace} property, not the DOM Element itself.
         * <p>This prevent potential corruption during Flash refreshes which may occur during DOM reflow or when the Flash object is not visible.
         * @param {String|Array} methods A single method name or Array of method names to bind.  Methods can be simple strings or objects of the form:<p>
         * {name:'methodName', returnType:'javascript'}.  Methods defined as strings default to a 'javascript' returnType.
         * @example flashObj.bindExternals(['Play', 'Stop', 'Rewind', {name: 'GetPlayList', returnType : 'xml'} ]);
         * flashMediaObj.Play();
         */
        bindExternals : function(methods){

            if(methods && this.playerVersion.major >= 8){
                methods = new Array().concat(methods);
            }else{
                return;
            }

            var nameSpace = (typeof this.externalsNamespace == 'string' ?
                  this[this.externalsNamespace] || (this[this.externalsNamespace] = {} )
                     : this );

            Ext.each(methods,function(method){

               var m = method.name || method;
               var returnType = method.returnType || 'javascript';

                //Do not overwrite existing function with the same name.
               nameSpace[m] || (nameSpace[m] = function(){
                      return this.invoke.apply(this,[m, returnType].concat(Array.prototype.slice.call(arguments,0)));
               }.createDelegate(this));

            },this);
        },

        /** Invoke a Flash ExternalInterface method
         * @param {String} method Method Name to invoke
         * @param {Mixed} arguments (Optional)  (1-n) method arguments.
         * @return {Mixed} Result (if provided) or, 'undefined' if the method
         * is not defined by the External Interface
         */
        invoke   : function(method , returnType /* , optional arguments, .... */ ){

            var obj,r;

            if(method && (obj = this.getInterface()) && 'CallFunction' in obj ){
                var c = [
                    String.format('<invoke name="{0}" returntype="{1}">',method, returnType),
                    '<arguments>',
                    (Array.prototype.slice.call(arguments,2)).map(this._toXML, this).join(''),
                    '</arguments>',
                    '</invoke>'].join('');
                
                r = obj.CallFunction(c);

                typeof r === 'string' && returnType ==='javascript' && (r= Ext.decode(r));

            }
            return r;

        },

        /**
         * this function is designed to be used when a Flashplayer player object notifies the browser
         * if its initialization state
         */
        onFlashInit  :  function(){

            if(this.mediaMask && this.autoMask){this.mediaMask.hide();}
            this.fireEvent.defer(300,this,['flashinit',this, this.getInterface()]);


        },

        /**  Flash Specific Method to synthesize a mediaload event
         * @private
         */
        pollReadyState : function(cb, readyRE){
            var media;

            if(media= this.getInterface()){
                if(typeof media.PercentLoaded != 'undefined'){
                   var perc = media.PercentLoaded() ;

                   this.fireEvent( 'progress' ,this , this.getInterface(), perc) ;
                   if( perc = 100 ) { cb(); return; }
                }

                this._countPoll++ > this._maxPoll || arguments.callee.defer(10,this,arguments);

            }

         },

        /**
         * @private
         * Dispatches events received from the SWF object (when defined by the eventSynch mediaConfig option).
         *
         * @method _handleSWFEvent
         * @private
         */
        _handleSWFEvent: function(event)
        {
            var type = event.type||event||false;
            if(type){
                 if(this.events && !this.events[String(type)])
                     { this.addEvents(String(type));}

                 return this.fireEvent.apply(this, [String(type), this].concat(Array.prototype.slice.call(arguments,0)));
            }
        },


       _toXML    : function(value){

           var format = Ext.util.Format;
           var type = typeof value;
           if (type == "string") {
               return "<string>" + format.xmlEncode(value) + "</string>";}
           else if (type == "undefined")
              {return "<undefined/>";}
           else if (type == "number")
              {return "<number>" + value + "</number>";}
           else if (value == null)
              {return "<null/>";}
           else if (type == "boolean")
              {return value ? "<true/>" : "<false/>";}
           else if (value instanceof Date)
              {return "<date>" + value.getTime() + "</date>";}
           else if (Ext.isArray(value))
              {return this._arrayToXML(value);}
           else if (type == "object")
              {return this._objectToXML(value);}
           else {return "<null/>";}
         },

        _arrayToXML  : function(arrObj){

            var s = "<array>";
            for (var i = 0,l = arrObj.length ; i < l; i++) {
                s += "<property id=\"" + i + "\">" + this._toXML(arrObj[i]) + "</property>";
            }
            return s + "</array>";
        },

        _objectToXML  : function(obj){

            var s = "<object>";
            for (var prop in obj) {
                if(obj.hasOwnProperty(prop)){
                   s += "<property id=\"" + prop + "\">" + this._toXML(obj[prop]) + "</property>";
                }
              }
            return s + "</object>";

        }

    });

    /**
     Class Method to handle defined Flash interface events
     @memberOf Ext.ux.Media.Flash
    */
    Ext.ux.Media.Flash.eventSynch = function(elementID, event /* additional arguments optional */ ){
            var SWF = Ext.get(elementID), inst;
            if(SWF && (inst = SWF.ownerCt)){
                return inst._handleSWFEvent.apply(inst, Array.prototype.slice.call(arguments,1));
            }
        };


    var componentAdapter = {
       init         : function(){

          this.getId = function(){
              return this.id || (this.id = "flash-comp" + (++Ext.Component.AUTO_ID));
          };

          this.addEvents(

             /**
              * Fires when the Flash Object reports an initialized state via a public callback function.
              * @event flashinit
              * @memberOf Ext.ux.Media.Flash
              * @param {Ext.ux.Media.Flash} this Ext.ux.Media.Flash instance
              * @param {Element} SWFObject The Flash object DOM Element.
              *
              * this callback must implemented to be useful in raising this event.
              * @example
              * //YouTube Global ready handler
                var onYouTubePlayerReady = function(playerId) {

                    //Search for a ux.Flash-managed player.
                    var flashComp, el = Ext.get(playerId);
                    if(flashComp = (el?el.ownerCt:null)){
                       flashComp.onFlashInit();
                    }

                };
              */
              'flashinit',

             /**
              * Fires when the Flash Object issues an fscommand to the ux.Flash Component
              * @event fscommand
              * @memberOf Ext.ux.Media.Flash
              * @param {Ext.ux.Media.Flash} this Ext.ux.Media.Flash instance
              * @param {string} command The command string
              * @param {string} args The arguments string
              */
              'fscommand',

             /**
              * Fires indicating the load progress of the Flash Movie Object
              * @event progress
              * @memberOf Ext.ux.Media.Flash
              * @param {Ext.ux.Media.Flash} this Ext.ux.Media.Flash instance
              * @param {Element} SWFObject The Flash object DOM Element.
              * @param {Integer} percent The percentage of the Movie loaded.
             */
             'progress' );

        }

    };


     /**
      *
      * @class Ext.ux.Media.Flash.Component
      * @extends Ext.ux.Media.Component
      * @base Ext.ux.Media.Flash
      * @version  2.1
      * @author Doug Hendricks. doug[always-At]theactivegroup.com
      * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
      * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
      * @license <a href="http://www.gnu.org/licenses/gpl.html">GPL 3.0</a>
      * @constructor

      * @param {Object} config The config object
      * @desc
      * Base Media Class for Flash objects
      * Used primarily for rendering Flash Objects for use with inline markup.
    */
   Ext.ux.Media.Flash.Component = Ext.extend(Ext.ux.Media.Component, {
         /**
         * @private
         */
         ctype         : "Ext.ux.Media.Flash.Component",


        /**
         * @private
         */
         cls    : "x-media-flash-comp",

         /**
         * @private
         */
         autoEl  : {tag:'div',style : { overflow: 'hidden', display:'block'}},

        /**
         * @private
         * The className of the Media interface to inherit
         */
         mediaClass    : Ext.ux.Media.Flash,

        /** @private */
         initComponent   : function(){

            componentAdapter.init.apply(this,arguments);
            Ext.ux.Media.Flash.Component.superclass.initComponent.apply(this,arguments);

         }



   });

   Ext.reg('uxflash', Ext.ux.Media.Flash.Component);

   ux.Flash.prototype.detectFlashVersion();

   /**
     *
     * @class Ext.ux.Media.Flash.Panel
     * @extends Ext.ux.Media.Panel
     * @version 2.1
     * @author Doug Hendricks. doug[always-At]theactivegroup.com
     * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
     * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
     * @constructor
     * @base Ext.ux.Media.Flash
     * @param {Object} config The config object
     * @desc
     * Base Media Class for Flash objects
     * Used primarily for rendering Flash Objects for use with inline markup.
    */

   Ext.ux.Media.Flash.Panel = Ext.extend(Ext.ux.Media.Panel,{

        ctype         : "Ext.ux.Media.Flash.Panel",

        mediaClass    : Ext.ux.Media.Flash,

        autoScroll    : false,

        /**
         * @cfg {Boolean} shadow Set to false to prevent DOM reflow when shadow is hidden/shown
         * @default false
         */
        shadow        : false,


        /** @private */
        initComponent   : function(){
            componentAdapter.init.apply(this,arguments);
            Ext.ux.Media.Flash.Panel.superclass.initComponent.apply(this,arguments);

       }

   });

   Ext.reg('flashpanel', ux.Flash.Panel);
   Ext.reg('uxflashpanel', ux.Flash.Panel);

   /**
    *
    * @class Ext.ux.Media.Flash.Portlet
    * @extends Ext.ux.Media.Portlet
    * @version  2.1
    * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
    * @author Doug Hendricks. doug[always-At]theactivegroup.com
    * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
    * @desc
    * Base Media Class for Flash objects
    * Used primarily for rendering Flash Objects for use with inline markup.
    */

   Ext.ux.Media.Flash.Portlet = Ext.extend(Ext.ux.Media.Portlet,{
       ctype         : "Ext.ux.Media.Flash.Portlet",
       anchor       : '100%',
       frame        : true,
       collapseEl   : 'bwrap',
       collapsible  : true,
       draggable    : true,
       autoScroll    : false,
       autoWidth    : true,
       cls          : 'x-portlet x-flash-portlet',
       mediaClass    : Ext.ux.Media.Flash,
       /** @private */
       initComponent   : function(){
           componentAdapter.init.apply(this,arguments);
           Ext.ux.Media.Flash.Panel.superclass.initComponent.apply(this,arguments);

       }

   });

   Ext.reg('flashportlet', ux.Flash.Portlet);
   Ext.reg('uxflashportlet', ux.Flash.Portlet);

   /**
    *
    * @class Ext.ux.Media.Flash.Window
    * @extends Ext.ux.Media.Window
    * @version  2.1
    * @author Doug Hendricks. doug[always-At]theactivegroup.com
    * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
    * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
    * @constructor
    * @base Ext.ux.Media.Flash
    * @param {Object} config The config object
    * @desc
    * Base Media Class for Flash objects
    * Used primarily for rendering Flash Objects for use with inline markup.
    */

   Ext.ux.Media.Flash.Window  = Ext.extend( Ext.ux.Media.Window , {

        ctype         : "Ext.ux.Media.Flash.Window",
        mediaClass    : Ext.ux.Media.Flash,

        autoScroll    : false,

        /**
         * @cfg {Boolean} shadow Set to false to prevent DOM reflow when shadow is hidden/shown
         * @default false
         */
        shadow        : false,


        /** @private */
        initComponent   : function(){
            componentAdapter.init.apply(this,arguments);
            Ext.ux.Media.Flash.Window.superclass.initComponent.apply(this,arguments);

       }

   });

   Ext.reg('flashwindow', ux.Flash.Window);

   /**
    *
    * @class Ext.ux.Media.Flash.Element
    * @extends Ext.ux.Media.Element
    * @version  2.1
    * @author Doug Hendricks. doug[always-At]theactivegroup.com
    * @donate <a target="tag_donate" href="http://donate.theactivegroup.com"><img border="0" src="http://www.paypal.com/en_US/i/btn/x-click-butcc-donate.gif" border="0" alt="Make a donation to support ongoing development"></a>
    * @copyright 2007-2009, Active Group, Inc.  All rights reserved.
    * @desc
    * Base Media Class for Flash objects
    * Used primarily for rendering Flash Objects for use with inline markup.
    */


   Ext.ux.Media.Flash.Element = Ext.extend ( Ext.ux.Media.Element , {

        /**
         * Removes this Flash element from the DOM and deletes it from the cache.  For Flash objects,
         * this requires special treatment to prevent Memory leakage (for IE).
         * @param {Boolean} cleanse (optional) Perform a cleanse of immediate childNodes as well.
         * @param {Boolean} deep (optional) Perform a deep cleanse of all nested childNodes as well.
         */

       remove : function(){

             var d ;
             // Fix streaming media troubles for IE
             // IE has issues with loose references when removing an <object>
             // before the onload event fires (all <object>s should have readyState == 4 after browsers onload)

             // Advice: do not attempt to remove the Component before onload has fired on IE/Win.

            if(Ext.isIE && Ext.isWindows && (d = this.dom)){

                this.removeAllListeners();
                d.style.display = 'none'; //hide it regardless of state
                if(d.readyState == 4){
                    for (var x in d) {
                        if (x.toLowerCase() != 'flashvars' && typeof d[x] == 'function') {
                            d[x] = null;
                        }
                    }
                }

             }

             Ext.ux.Media.Flash.Element.superclass.remove.apply(this, arguments);

         }

   });

   Ext.ux.Media.Flash.prototype.elementClass  =  Ext.ux.Media.Flash.Element;

   var writeScript = function(block, attributes) {
        attributes = Ext.apply({},attributes||{},{type :"text/javascript",text:block});

         try{
            var head,script, doc= document;
            if(doc && doc.getElementsByTagName){
                if(!(head = doc.getElementsByTagName("head")[0] )){

                    head =doc.createElement("head");
                    doc.getElementsByTagName("html")[0].appendChild(head);
                }
                if(head && (script = doc.createElement("script"))){
                    for(var attrib in attributes){
                          if(attributes.hasOwnProperty(attrib) && attrib in script){
                              script[attrib] = attributes[attrib];
                          }
                    }
                    return !!head.appendChild(script);
                }
            }
         }catch(ex){}
         return false;
    };

    /* This is likely unnecessary with this implementation, as Flash objects are removed as needed
     * during the clearMedia method, but included to cleanup inline flash markup.
     */
    if(Ext.isIE && Ext.isWindows && ux.Flash.prototype.flashVersion.major == 9) {

        window.attachEvent('onbeforeunload', function() {
              __flash_unloadHandler = __flash_savedUnloadHandler = function() {};
        });

        //Note: we cannot use IE's onbeforeunload event because an internal Flash Form-POST
        // raises the browsers onbeforeunload event when the server returns a response.  that is crazy!
        window.attachEvent('onunload', function() {

            Ext.each(Ext.query('.x-media-swf'), function(item, index) {
                item.style.display = 'none';
                for (var x in item) {
                    if (x.toLowerCase() != 'flashvars' && typeof item[x] == 'function') {
                        item[x] = null;
                    }
                }
            });
        });

    }

 Ext.apply(Ext.util.Format , {
       /**
         * Convert certain characters (&, <, >, and ') to their HTML character equivalents for literal display in web pages.
         * @param {String} value The string to encode
         * @return {String} The encoded text
         */
        xmlEncode : function(value){
            return !value ? value : String(value)
                .replace(/&/g, "&amp;")
                .replace(/>/g, "&gt;")
                .replace(/</g, "&lt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&apos;");
        },

        /**
         * Convert certain characters (&, <, >, and ') from their HTML character equivalents.
         * @param {String} value The string to decode
         * @return {String} The decoded text
         */
        xmlDecode : function(value){
            return !value ? value : String(value)
                .replace(/&gt;/g, ">")
                .replace(/&lt;/g, "<")
                .replace(/&quot;/g, '"')
                .replace(/&amp;/g, "&")
                .replace(/&apos;/g, "'");

        }

    });


 Ext.ux.FlashComponent  = Ext.ux.Media.Flash.Component ;
 Ext.ux.FlashPanel      = Ext.ux.Media.Flash.Panel;
 Ext.ux.FlashPortlet    = Ext.ux.Media.Flash.Portlet;
 Ext.ux.FlashWindow     = Ext.ux.Media.Flash.Window;

})();


(function() {

	/**
	 * A class that works as index of all proxies urls
	 */
	Ext.define('CMDBuild.core.proxy.CMProxyUrlIndex', {
		alternateClassName: 'CMDBuild.ServiceProxy.url', // Legacy class name

		statics: {
			attribute: {
				create: '',
				read: 'services/json/schema/modclass/getattributelist',
				update: 'services/json/schema/modclass/saveattribute',
				remove: 'services/json/schema/modclass/deleteattribute',

				reorder: 'services/json/schema/modclass/reorderattribute',
				updateSortConfiguration: 'services/json/schema/modclass/saveordercriteria'
			},

			basicCardList: 'services/json/management/modcard/getcardlistshort',

			card: {
				create: '',
				read: 'services/json/management/modcard/getcard',
				update: '',
				remove: 'services/json/management/modcard/deletecard',

				bulkUpdate: 'services/json/management/modcard/bulkupdate',
				bulkUpdateFromFilter: 'services/json/management/modcard/bulkupdatefromfilter',
				getPosition: 'services/json/management/modcard/getcardposition',
				lock: 'services/json/management/modcard/lockcard',
				unlock: 'services/json/management/modcard/unlockcard',
				unlockAll: 'services/json/management/modcard/unlockallcards'
			},

			cardList: 'services/json/management/modcard/getcardlist',

			classes: {
				create: 'services/json/schema/modclass/savetable',
				read: 'services/json/schema/modclass/getallclasses',
				update: 'services/json/schema/modclass/savetable',
				remove: 'services/json/schema/modclass/deletetable'
			},

			domain: {
				create: 'services/json/schema/modclass/savedomain',
				read: 'services/json/schema/modclass/getalldomains',
				update: 'services/json/schema/modclass/savedomain',
				remove: 'services/json/schema/modclass/deletedomain'
			},

			navigationTrees: {
				get: 'services/json/navigationtree/get',
				read: 'services/json/navigationtree/read',
				create: 'services/json/navigationtree/create',
				save: 'services/json/navigationtree/save',
				remove: 'services/json/navigationtree/remove'
			},

			dataView: {
				read: 'services/json/viewmanagement/read',
				filter: {
					create: 'services/json/viewmanagement/createfilterview',
					read: 'services/json/viewmanagement/readfilterview',
					update: 'services/json/viewmanagement/updatefilterview',
					remove: 'services/json/viewmanagement/deletefilterview'
				},
				sql: {
					create: 'services/json/viewmanagement/createsqlview',
					read: 'services/json/viewmanagement/readsqlview',
					update: 'services/json/viewmanagement/updatesqlview',
					remove: 'services/json/viewmanagement/deletesqlview'
				}
			},

			email: {
				accounts:{
					delete: 'services/json/schema/emailaccount/delete',
					get: 'services/json/schema/emailaccount/get',
					post: 'services/json/schema/emailaccount/post',
					put: 'services/json/schema/emailaccount/put',

					getStore: 'services/json/schema/emailaccount/getall',
					getStoreColumns: '',
					setDefault: 'services/json/schema/emailaccount/setdefault'
				},
				templates:{
					delete: 'services/json/emailtemplate/deletetemplate',
					get: 'services/json/emailtemplate/readtemplate',
					post: 'services/json/emailtemplate/createtemplate',
					put: 'services/json/emailtemplate/updatetemplate',

					getStore: 'services/json/emailtemplate/readtemplates'
				}
			},

			fkTargetClass: 'services/json/schema/modclass/getfktargetingclass',

			filter: {
				read: 'services/json/filter/read',
				create: 'services/json/filter/create',
				update: 'services/json/filter/update',
				remove: 'services/json/filter/delete',

				position: 'services/json/filter/position',
				userStore: 'services/json/filter/readforuser',
				groupStore: 'services/json/filter/readallgroupfilters'
			},

			functions: {
				getFunctions: 'services/json/schema/modclass/getfunctions'
			},

			login: 'services/json/login/login',

			menu: {
				create: '',
				read: 'services/json/schema/modmenu/getassignedmenu',
				update: 'services/json/schema/modmenu/savemenu',
				remove: 'services/json/schema/modmenu/deletemenu',

				readConfiguration: 'services/json/schema/modmenu/getmenuconfiguration',
				readAvailableItems: 'services/json/schema/modmenu/getavailablemenuitems'
			},

			privileges: {
				classes: {
					read: 'services/json/schema/modsecurity/getclassprivilegelist',
					update: 'services/json/schema/modsecurity/saveclassprivilege',

					clearRowAndColumnPrivileges: 'services/json/schema/modsecurity/clearrowandcolumnprivileges',
					setRowAndColumnPrivileges: 'services/json/schema/modsecurity/setrowandcolumnprivileges',
					saveClassUiConfiguration: 'services/json/schema/modsecurity/saveclassuiconfiguration',
					loadClassUiConfiguration: 'services/json/schema/modsecurity/loadclassuiconfiguration'
				},
				dataView: {
					read: 'services/json/schema/modsecurity/getviewprivilegelist',
					update: 'services/json/schema/modsecurity/saveviewprivilege'
				},
				filter: {
					read: 'services/json/schema/modsecurity/getfilterprivilegelist',
					update: 'services/json/schema/modsecurity/savefilterprivilege'
				}
			},

			tasks: {
				getStore: 'services/json/schema/taskmanager/readall',
				start: 'services/json/schema/taskmanager/start',
				stop: 'services/json/schema/taskmanager/stop',

				connector: {
					delete: '',
					get: '',
					post: '',
					put: '',

					getStore: 'services/json/schema/taskmanager/connector/readall',
				},
				email: {
					delete: 'services/json/schema/taskmanager/reademail/delete',
					get: 'services/json/schema/taskmanager/reademail/read',
					post: 'services/json/schema/taskmanager/reademail/create',
					put: 'services/json/schema/taskmanager/reademail/update',

					getStore: 'services/json/schema/taskmanager/reademail/readall'
				},
				event: {
					getStore: 'services/json/schema/taskmanager/event/readall',

					asynchronous: {
						delete: '',
						get: '',
						post: '',
						put: '',

						getStore: 'services/json/schema/taskmanager/event/readall',
					},
					synchronous: {
						delete: '',
						get: '',
						post: '',
						put: '',

						getStore: 'services/json/schema/taskmanager/event/readall',
					}
				},
				workflow: {
					delete: 'services/json/schema/taskmanager/startworkflow/delete',
					get: 'services/json/schema/taskmanager/startworkflow/read',
					post: 'services/json/schema/taskmanager/startworkflow/create',
					put: 'services/json/schema/taskmanager/startworkflow/update',

					getStore: 'services/json/schema/taskmanager/startworkflow/readall',
					getStoreByWorkflow: 'services/json/schema/taskmanager/startworkflow/readallbyworkflow'
				}
			},

			workflow: {
				abortProcess: 'services/json/workflow/abortprocess',
				getStartActivity: 'services/json/workflow/getstartactivity',
				getActivityInstance: 'services/json/workflow/getactivityinstance',
				isProcessUpdated: 'services/json/workflow/isprocessupdated',
				saveActivity: 'services/json/workflow/saveactivity'
			}
		}
	});

})();
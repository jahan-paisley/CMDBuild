package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class ManageRelationToolAgent extends AbstractConditionalToolAgent {

	private static enum Operation {

		CREATION, DELETION, SELECTION;

		private static final String CREATE_PREFIX = "create";
		private static final String DELETE_PREFIX = "delete";
		private static final String SELECT_PREFIX = "select";

		public static Operation from(final String id) {
			final Operation operation;
			if (id.startsWith(CREATE_PREFIX)) {
				operation = CREATION;
			} else if (id.startsWith(DELETE_PREFIX)) {
				operation = DELETION;
			} else if (id.startsWith(SELECT_PREFIX)) {
				operation = SELECTION;
			} else {
				throw new IllegalArgumentException("invalid id");
			}
			return operation;
		}

	}

	private static abstract class Executor {

		private boolean exeptionCatched;

		public void execute() throws Exception {
			try {
				doExecute();
			} catch (final Exception e) {
				exeptionCatched = true;
				if (throwCatchedExeption()) {
					throw e;
				}
			}
		}

		protected boolean exeptionCatched() {
			return exeptionCatched;
		}

		protected abstract void doExecute();

		protected abstract boolean throwCatchedExeption();

		public abstract Object getReturnValue();

	}

	private static final String SIDE_1 = "1";
	private static final String SIDE_2 = "2";

	private static final String DOMAIN_NAME = "DomainName";
	private static final String CLASS_NAME_BASE = "ClassName";
	private static final String CARD_ID_BASE = "CardId";
	private static final String OBJ_ID_BASE = "ObjId";
	private static final String OBJ_REFERENCE_BASE = "ObjReference";
	private static final String REF_BASE = "Ref";

	@Override
	protected void innerInvoke() throws Exception {
		final Executor executor = executorFromToolId();
		executor.execute();
		final Object outValue = executor.getReturnValue();
		for (final AppParameter parmOut : getReturnParameters()) {
			if (parmOut.the_class == outValue.getClass()) {
				parmOut.the_value = outValue;
			}
		}
	}

	protected Executor executorFromToolId() {
		final Operation operation = Operation.from(getId());
		final String domainName = getParameterValue(DOMAIN_NAME);
		final Executor executor;
		switch (operation) {
		case CREATION:
			executor = creationExecutor(domainName);
			break;

		case DELETION:
			executor = deletionExecutor(domainName);
			break;

		case SELECTION:
			executor = selectionExecutor(domainName);
			break;

		default:
			executor = illegalExecutor(operation);
		}
		return executor;
	}

	protected Executor creationExecutor(final String domainName) {
		return new Executor() {

			@Override
			protected void doExecute() {
				final CardRef card1 = getCard1();
				final CardRef card2 = getCard2();
				getWorkflowApi().newRelation(domainName) //
						.withCard1(card1.className, card1.cardId) //
						.withCard2(card2.className, card2.cardId) //
						.create();
			}

			@Override
			protected boolean throwCatchedExeption() {
				return false;
			}

			@Override
			public Object getReturnValue() {
				return !exeptionCatched();
			}

		};
	}

	protected Executor deletionExecutor(final String domainName) {
		return new Executor() {

			@Override
			protected void doExecute() {
				final CardRef card1 = getCard1();
				final CardRef card2 = getCard2();
				getWorkflowApi().existingRelation(domainName) //
						.withCard1(card1.className, card1.cardId) //
						.withCard2(card2.className, card2.cardId) //
						.delete();
			}

			@Override
			protected boolean throwCatchedExeption() {
				return false;
			}

			@Override
			public Object getReturnValue() {
				return !exeptionCatched();
			}

		};
	}

	protected Executor selectionExecutor(final String domainName) {
		return new Executor() {

			private ReferenceType[] referenceTypes;

			@Override
			protected void doExecute() {
				final CardRef card = getCard();
				final List<CardDescriptor> descriptors = getWorkflowApi().queryRelations(card.className, card.cardId) //
						.withDomain(domainName) //
						.fetch();
				referenceTypes = referenceTypeFor(descriptors);
			}

			private ReferenceType[] referenceTypeFor(final List<CardDescriptor> descriptors) {
				final List<ReferenceType> referenceTypes = new ArrayList<ReferenceType>();
				for (final CardDescriptor descriptor : descriptors) {
					referenceTypes.add( //
							getWorkflowApi().referenceTypeFrom( //
									cardWithEmptyDescriptionFrom(descriptor)));
				}
				return referenceTypes.toArray(new ReferenceType[referenceTypes.size()]);
			}

			private ExistingCard cardWithEmptyDescriptionFrom(final CardDescriptor descriptor) {
				// used for avoid to query CMDBuild for card
				// description
				return getWorkflowApi().existingCard(descriptor).withDescription(EMPTY);
			}

			@Override
			protected boolean throwCatchedExeption() {
				return true;
			}

			@Override
			public Object getReturnValue() {
				return referenceTypes;
			}

		};
	}

	protected Executor illegalExecutor(final Operation operation) {
		return new Executor() {

			@Override
			protected void doExecute() {
				final String message = format("illegal operation '%s'", operation);
				throw new IllegalArgumentException(message);
			}

			@Override
			protected boolean throwCatchedExeption() {
				return true;
			}

			@Override
			public Object getReturnValue() {
				return null;
			}

		};
	}

	private CardRef getCard() {
		return getCard(CLASS_NAME_BASE, CARD_ID_BASE, REF_BASE, EMPTY);
	}

	private CardRef getCard1() {
		return getCard(CLASS_NAME_BASE, OBJ_ID_BASE, OBJ_REFERENCE_BASE, SIDE_1);
	}

	private CardRef getCard2() {
		return getCard(CLASS_NAME_BASE, OBJ_ID_BASE, OBJ_REFERENCE_BASE, SIDE_2);
	}

	private CardRef getCard(final String classNameBase, final String cardIdBase, final String referenceBase,
			final String suffix) {
		final String className;
		final int cardId;
		if (hasParameter(classNameBase + suffix)) {
			className = getParameterValue(classNameBase + suffix);
			final Long objId = getParameterValue(cardIdBase + suffix);
			cardId = objId.intValue();
		} else {
			final ReferenceType objReference = getParameterValue(referenceBase + suffix);
			className = getWorkflowApi().findClass(objReference.getIdClass()).getName();
			cardId = objReference.getId();
		}
		return new CardRef(className, cardId);
	}

}

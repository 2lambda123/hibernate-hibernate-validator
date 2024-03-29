// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.constraints.impl;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.impl.ScriptAssertValidator;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link org.hibernate.validator.constraints.impl.ScriptAssertValidator}.
 *
 * @author Gunnar Morling
 */
public class ScriptAssertValidatorTest {

	@Test
	public void scriptEvaluatesToTrue() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "javascript", "true" );

		assertTrue( validator.isValid( new Object(), null ) );
	}

	@Test
	public void scriptEvaluatesToFalse() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "javascript", "false" );

		assertFalse( validator.isValid( new Object(), null ) );
	}

	@Test
	public void scriptExpressionReferencingAnnotatedObject() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator(
				"javascript", "_this.startDate.before(_this.endDate)"
		);

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		assertTrue( validator.isValid( new CalendarEvent( startDate, endDate ), null ) );
		assertFalse( validator.isValid( new CalendarEvent( endDate, startDate ), null ) );
	}

	@Test
	public void scriptExpressionUsingCustomizedAlias() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator(
				"javascript", "_.startDate.before(_.endDate)", "_"
		);

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		assertFalse( validator.isValid( new CalendarEvent( endDate, startDate ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyLanguageNameRaisesException() throws Exception {

		getInitializedValidator( "", "script" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyScriptRaisesException() throws Exception {

		getInitializedValidator( "lang", "" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyAliasRaisesException() throws Exception {

		getInitializedValidator( "lang", "script", "" );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void unknownLanguageNameRaisesException() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "foo", "script" );

		validator.isValid( new Object(), null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void illegalScriptExpressionRaisesException() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "javascript", "foo" );

		validator.isValid( new Object(), null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void scriptExpressionReturningNullRaisesException() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator( "javascript", "null" );

		validator.isValid( new Object(), null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class)
	public void scriptExpressionReturningNoBooleanRaisesException() throws Exception {

		ConstraintValidator<ScriptAssert, Object> validator = getInitializedValidator(
				"javascript", "new java.util.Date()"
		);

		validator.isValid( new Object(), null );
	}

	/**
	 * Returns a {@link org.hibernate.validator.constraints.impl.ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script, String name) {

		ConstraintValidator<ScriptAssert, Object> validator = new ScriptAssertValidator();
		validator.initialize( getScriptAssert( lang, script, name ) );

		return validator;
	}

	/**
	 * Returns a {@link ScriptAssertValidator} initialized with a {@link ScriptAssert} with the given values.
	 */
	private ConstraintValidator<ScriptAssert, Object> getInitializedValidator(String lang, String script) {

		ConstraintValidator<ScriptAssert, Object> validator = new ScriptAssertValidator();
		validator.initialize( getScriptAssert( lang, script, null ) );

		return validator;
	}

	/**
	 * Returns a {@link ScriptAssert} initialized with the given values.
	 */
	private ScriptAssert getScriptAssert(String lang, String script, String name) {

		AnnotationDescriptor<ScriptAssert> descriptor = AnnotationDescriptor.getInstance( ScriptAssert.class );

		descriptor.setValue( "lang", lang );
		descriptor.setValue( "script", script );
		if ( name != null ) {
			descriptor.setValue( "alias", name );
		}

		return AnnotationFactory.create( descriptor );
	}

	/**
	 * An exemplary model class used in tests.
	 *
	 * @author Gunnar Morling
	 */
	private static class CalendarEvent {

		private Date startDate;

		private Date endDate;

		public CalendarEvent(Date startDate, Date endDate) {

			this.startDate = startDate;
			this.endDate = endDate;
		}

		@SuppressWarnings("unused")
		public Date getStartDate() {
			return startDate;
		}

		@SuppressWarnings("unused")
		public Date getEndDate() {
			return endDate;
		}

	}
}

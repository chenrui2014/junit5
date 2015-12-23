/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.stream.Collectors.toSet;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.io.File;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.TestPlanSpecification;

class TestPlanSpecificationCreator {

	TestPlanSpecification toTestPlanSpecification(CommandLineOptions options) {
		TestPlanSpecification specification = buildSpecification(options);
		applyFilters(specification, options);
		return specification;
	}

	private TestPlanSpecification buildSpecification(CommandLineOptions options) {
		if (options.isRunAllTests()) {
			return buildAllTestsSpecification(options);
		}
		return buildNameBasedSpecification(options);
	}

	private TestPlanSpecification buildAllTestsSpecification(CommandLineOptions options) {
		Set<File> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return build(allTests(rootDirectoriesToScan));
	}

	private Set<File> determineClasspathRootDirectories(CommandLineOptions options) {
		if (options.getArguments().isEmpty()) {
			return ReflectionUtils.getAllClasspathRootDirectories();
		}
		return options.getArguments().stream().map(File::new).collect(toSet());
	}

	private TestPlanSpecification buildNameBasedSpecification(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return build(TestPlanSpecification.forNames(options.getArguments()));
	}

	private void applyFilters(TestPlanSpecification specification, CommandLineOptions options) {
		options.getClassnameFilter().ifPresent(regex -> specification.filterWith(classNameMatches(regex)));
		if (!options.getTagsFilter().isEmpty()) {
			specification.filterWith(byTags(options.getTagsFilter()));
		}
		if (!options.getExcludeTags().isEmpty()) {
			specification.filterWith(excludeTags(options.getExcludeTags()));
		}
	}
}

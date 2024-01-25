package org.quiltmc.enigma.impl.translation.mapping;

import org.quiltmc.enigma.util.validation.StandardValidation;
import org.quiltmc.enigma.util.validation.ValidationContext;

public final class IdentifierValidation {
	private IdentifierValidation() {
	}

	private static final String ILLEGAL_CHARS = ".;[/<>:";

	public static boolean validateClassName(ValidationContext vc, String name, boolean isInner) {
		if (!StandardValidation.notBlank(vc, name)) return false;

		if (isInner) {
			// When renaming, inner class names do not contain the package name,
			// but only the class name.
			return validateIdentifier(vc, name);
		}

		String[] parts = name.split("/");
		for (String part : parts) {
			validateIdentifier(vc, part);
		}

		return true;
	}

	public static boolean validateIdentifier(ValidationContext vc, String name) {
		if (!StandardValidation.notBlank(vc, name)) return false;
		for (int i = 0; i < name.length(); i++) {
			if (ILLEGAL_CHARS.indexOf(name.charAt(i)) != -1) {
				return false;
			}
		}
		return true;
	}

	public static boolean isReservedMethodName(String name) {
		return false;
	}
}

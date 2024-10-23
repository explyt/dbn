package com.dbn.object;

import com.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;


public interface DBJavaObject extends DBSchemaObject {

	@NotNull
	String getName();

	boolean isFinal();

	boolean isAbstract();

	boolean isStatic();

	boolean isInner();

	String getKind();
// {CLASS, ENUM, INTERFACE}

	String getAccessibility();
// { PUBLIC, PRIVATE, PROTECTED, NULL}
}

/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.common.util;

import lombok.experimental.UtilityClass;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

@UtilityClass
public class Writers {

    public static BufferedWriter buffered(OutputStream stream) {
        return new BufferedWriter(new OutputStreamWriter(stream));
    }

    public static BufferedWriter buffered(OutputStream stream, Charset charset) {
        return new BufferedWriter(new OutputStreamWriter(stream, charset));
    }
}

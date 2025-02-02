/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef RUNTIME_RUNTIME_H
#define RUNTIME_RUNTIME_H

#include "Porting.h"

struct InitNode;

#ifdef __cplusplus
extern "C" {
#endif

// For experimental MM, if runtime gets initialized, it will be in the native state after this.
RUNTIME_NOTHROW void Kotlin_initRuntimeIfNeeded();
void Kotlin_deinitRuntimeIfNeeded();

// Can only be called once.
// No new runtimes can be initialized on any thread after this.
// Must be called on a thread with active runtime.
// Using already initialized runtimes on any thread after this is undefined behaviour.
void Kotlin_shutdownRuntime();

// Appends given node to an initializer list.
void AppendToInitializersTail(struct InitNode*);

void CallInitGlobalPossiblyLock(int* state, void (*init)());
void CallInitThreadLocal(int volatile* globalState, int* localState, void (*init)());

bool Kotlin_memoryLeakCheckerEnabled();

bool Kotlin_cleanersLeakCheckerEnabled();

bool Kotlin_forceCheckedShutdown();

#ifdef __cplusplus
} // extern "C"
#endif
#endif // RUNTIME_RUNTIME_H

/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.codegen.BlockInfo
import org.jetbrains.kotlin.backend.jvm.codegen.ExpressionCodegen
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.AbstractTypeMapper
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import java.lang.IllegalStateException

abstract class Range(private val kind: RangeKind) : IntrinsicMethod() {
    private fun mapRangeTypeToPrimitiveType(rangeType: Type, kind: RangeKind): Type {
        val fqName = rangeType.internalName
        return when (fqName.substringAfter("kotlin/ranges/").substringBefore("Range")) {
            "Double" -> Type.DOUBLE_TYPE
            "Float" -> Type.FLOAT_TYPE
            "Long" -> Type.LONG_TYPE
            "Int" -> Type.INT_TYPE
            "Short" -> Type.SHORT_TYPE
            "Char" -> Type.CHAR_TYPE
            "Byte" -> Type.BYTE_TYPE
            else -> throw IllegalStateException("${kind.desc} intrinsic can only work for primitive types: $fqName")
        }
    }

    override fun toCallable(
        expression: IrFunctionAccessExpression, signature: JvmMethodSignature, context: JvmBackendContext
    ): IrIntrinsicFunction {
        val argType = mapRangeTypeToPrimitiveType(signature.returnType, kind)
        return object : IrIntrinsicFunction(expression, signature, context, listOf(argType) + signature.valueParameters.map { argType }) {
            override fun genInvokeInstruction(v: InstructionAdapter) {
                v.invokespecial(
                    signature.returnType.internalName,
                    "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE, argType, argType),
                    false
                )
            }

            override fun invoke(
                v: InstructionAdapter,
                codegen: ExpressionCodegen,
                data: BlockInfo,
                expression: IrFunctionAccessExpression
            ): StackValue {
                codegen.markLineNumber(expression)
                v.anew(returnType)
                v.dup()
                return super.invoke(v, codegen, data, expression)
            }
        }
    }

    enum class RangeKind(val desc: String) { TO("RangeTo"), UNTIL("RangeUntil") }
}

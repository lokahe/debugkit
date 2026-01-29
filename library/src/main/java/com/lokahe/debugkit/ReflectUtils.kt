package com.lokahe.debugkit

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.lokahe.debugkit.ReflectUtils.Companion.TAG
import com.lokahe.debugkit.ReflectUtils.Companion.checkArgsForInvokeMethod
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method

@SuppressLint("DiscouragedPrivateApi")
fun AccessibleObject.makeAccessible() {
    try {
        this.isAccessible = true
        AccessibleObject::class.java.getDeclaredField("override").let {
            it.isAccessible = true
            it.get(this) as Boolean
        }
    } catch (e: NoSuchFieldException) {
        e.message?.let { Log.e(TAG, it) }
    } catch (e: IllegalAccessException) {
        e.message?.let { Log.e(TAG, it) }
    }
}

fun AccessibleObject.makeUnaccessible() {
    this.isAccessible = false
}

fun Class<*>.fields(): List<Field> {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (HiddenApiBypass.getInstanceFields(this) + HiddenApiBypass.getStaticFields(this)).let { fields ->
                return fields
            }
        } else {
            getClassLoader()?.let { loader ->
                return loader.loadClass(getName()).declaredFields.toList()
            }
        }
    } catch (e: ClassNotFoundException) {
        e.message?.let { Log.e(TAG, it) }
    }
    return emptyList()
}

fun Class<*>.executables(): List<Executable> {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.getDeclaredMethods(this).let { methods ->
                return methods
            }
        }
        getClassLoader()?.let { loader ->
            loader.loadClass(getName()).let { cls ->
                return (cls.declaredMethods.toList() + cls.declaredConstructors.toList())
            }
        }
    } catch (e: ClassNotFoundException) {
        e.message?.let { Log.e(TAG, it) }
    }
    return emptyList()
}

fun Class<*>.findField(fieldName: String): Field? {
    return fields().find { it.name == fieldName }
}

fun Class<*>.findExecutable(methodName: String, vararg parameterTypes: Class<*>): Executable? {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    return executables().find {
        it.name == methodName && it.parameterTypes.contentEquals(parameterTypes)
    }
//    }
//    getClassLoader()?.let { loader ->
//        try {
//            loader.loadClass(getName()).let { clazz ->
//                return clazz.getDeclaredMethod(methodName, *parameterTypes)
//                    ?: clazz.getDeclaredConstructor(*parameterTypes)
//            }
//        } catch (e: ClassNotFoundException) {
//            e.message?.let { Log.e(TAG, it) }
//        }
//    }
//    return null
}

fun Class<*>.findExecutable(methodName: String, vararg args: Any?): Executable? {
    return this.executables().find {
        it.name == methodName && checkArgsForInvokeMethod(it.parameterTypes, arrayOf(*args))
    }
}

fun Class<*>.execute(methodName: String, obj: Any? = null, vararg args: Any?): Any? {
    var ret: Any? = null
    if(BuildConfig.DEBUG) Log.d(TAG, "execute $simpleName $methodName with ${args.toList()}")
    this.findExecutable(methodName, *args)?.let { executable ->
        executable.makeAccessible()
        try {
            when (executable) {
                is Constructor<*> -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ret = HiddenApiBypass.newInstance(this, *args)
                    } else {
                        ret = executable.newInstance(*args)
                    }
                }

                is Method -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ret = HiddenApiBypass.invoke(this, obj, methodName, *args)
                    } else {
                        ret = executable.invoke(obj, *args)
                    }
                }

                else -> {}
            }
        } catch (e: Exception) {
            e.message?.let { msg -> Log.e(TAG, msg) }
        } finally {
            executable.makeUnaccessible()
        }
    }
    return ret
}

fun Field.value(obj: Any? = null): Any? = if (isStatic) get(null) else obj?.let { get(it) }

val Field.modifierString: String
    get() = java.lang.reflect.Modifier.toString(this.modifiers)

val Field.isStatic: Boolean
    get() = java.lang.reflect.Modifier.isStatic(this.modifiers)

val Executable.modifierString: String
    get() = java.lang.reflect.Modifier.toString(this.modifiers)

val Executable.returnType: Class<*>
    get() = when (this) {
        is Method -> this.returnType
        is Constructor<*> -> this.declaringClass
        else -> Void.TYPE // Fallback
    }

class ReflectUtils {

    companion object {
        val TAG: String = ReflectUtils::class.java.simpleName

        /**
         * Log fields of a class or an object.
         * @param obj The object to log fields from. null to log only static field values.
         * @param clazz The class to log fields from. null to log obj's class.
         * @param fieldName The name of the field to log. null to log all fields.
         * @param tag The tag to use for logging.
         */
        @JvmStatic
        @JvmOverloads
        fun logFields(
            obj: Any? = null,
            clazz: Class<*>? = null,
            fieldName: String? = null,
            tag: String = TAG
        ) {
            (clazz ?: obj?.javaClass)?.let { cls ->
                cls.fields().filter { field -> fieldName?.let { field.name == it } ?: true }
                    .let { fields ->
                        try {
                            logD(
                                tag, { "${cls.simpleName} field: " },
                                fields.map { field -> field.name },
                                fields.map { field -> field.type.simpleName },
                                fields.map { field -> field.modifierString },
                                fields.map { field ->
                                    field.makeAccessible()
                                    field.value(obj).toString()
                                }
                            )
                        } catch (e: IllegalAccessException) {
                            e.message?.let { msg -> Log.e(TAG, msg) }
                        } finally {
                            fields.forEach { field -> field.makeUnaccessible() }
                        }
                    }
            }
        }

        /**
         * Log methods of a class or an object.
         * @param obj The object to log methods from. null to log only static methods.
         * @param clazz The class to log methods from. null to log obj's class.
         * @param executableName The name of the method to log. null to log all methods.
         * @param tag The tag to use for logging.
         */
        @JvmStatic
        @JvmOverloads
        fun logExecutables(
            obj: Any? = null,
            clazz: Class<*>? = null,
            executableName: String? = null,
            tag: String = TAG
        ) {
            (clazz ?: obj?.javaClass)?.let { cls ->
                cls.executables()
                    .filter { executable -> executableName?.let { executable.name == it } ?: true }
                    .let { executables ->
                        logD(
                            tag, {
                                "${cls.simpleName} ${
                                    when (executables[it]) {
                                        is Method -> "method"
                                        is Constructor<*> -> "constructor"
                                        else -> "executable"
                                    }
                                }: "
                            },
                            executables.map { executable -> executable.name },
                            executables.map { executable -> executable.returnType.simpleName },
                            executables.map { executable -> executable.modifierString },
                            executables.map { executable ->
                                executable.makeAccessible()
                                executable.parameterTypes.joinToString()
                            }
                        )
                        executables.forEach { executable -> executable.makeUnaccessible() }
                    }
            }
        }

        /**
         * Get a field of a class or an object.
         * @param fieldName The name of the field to get.
         * @param obj The object to get the field from. null to get static field values.
         * @param clazz The class to get the field from. null to get obj's class.
         * @return The value of the field. null if the field is not found.
         */
        @JvmStatic
        @JvmOverloads
        fun getField(fieldName: String, obj: Any? = null, clazz: Class<*>? = null): Any? {
            (clazz ?: obj?.javaClass)?.findField(fieldName)?.let { field ->
                field.makeAccessible()
                try {
                    return field.value(obj)
                } catch (e: IllegalAccessException) {
                    e.message?.let { Log.e(TAG, it) }
                } finally {
                    field.makeUnaccessible()
                }
            }
            return null
        }

        /**
         * Set a field of a class or an object.
         * @param fieldName The name of the field to set.
         * @param value The value to set the field to.
         * @param obj The object to set the field on. null to set static field values.
         * @param clazz The class to set the field on. null to set obj's class.
         * @return The value of the field. null if the field is not found.
         */
        @JvmStatic
        @JvmOverloads
        fun setField(fieldName: String, value: Any?, obj: Any? = null, clazz: Class<*>? = null) {
            (clazz ?: obj?.javaClass)?.findField(fieldName)?.let { field ->
                try {
                    field.makeAccessible()
                    if (!field.isStatic && obj == null) {
                        throw IllegalArgumentException("obj is null for non-static field")
                    }
                    field.set(obj, value)
                } catch (e: IllegalAccessException) {
                    e.message?.let { Log.e(TAG, it) }
                } catch (e: IllegalArgumentException) {
                    e.message?.let { Log.e(TAG, it) }
                } finally {
                    field.makeUnaccessible()
                }
            }
        }

        /**
         * Get an executable of a class or an object.
         * @param methodName The name of the method to get.
         * @param obj The object to get the method from. null to get static methods.
         * @param clazz The class to get the method from. null to get obj's class.
         * @param parameterTypes The parameter types of the method.
         */
        @JvmStatic
        @JvmOverloads
        fun getExecutable(
            methodName: String,
            obj: Any? = null,
            clazz: Class<*>? = null,
            vararg parameterTypes: Class<*>
        ): Executable? = (clazz ?: obj?.javaClass)?.findExecutable(methodName, *parameterTypes)

        /**
         * Invoke an executable of a class or an object.
         * @param name The name of the method to invoke.
         * @param obj The object to invoke the method on. null to invoke static methods.
         * @param clazz The class to invoke the method on. null to invoke obj's class.
         * @param args The arguments to pass to the method.
         * @return The return value of the method. null if the method is not found.
         */
        @JvmStatic
        @JvmOverloads
        fun invoke(
            name: String,
            obj: Any? = null,
            clazz: Class<*>? = null,
            vararg args: Any?
        ): Any? {
            return (clazz ?: obj?.javaClass)?.execute(name, obj, *args)
        }

        /**
         * Create a new instance of a class.
         * @param clazz The class to create a new instance of.
         * @param args The arguments to pass to the constructor.
         * @return The new instance of the class. null if the constructor is not found.
         */
        @JvmStatic
        fun newInstance(clazz: Class<*>, vararg args: Any?): Any? {
            return HiddenApiBypass.newInstance(clazz, *args)
        }

        internal fun checkArgsForInvokeMethod(
            params: Array<Class<*>?>,
            args: Array<Any?>
        ): Boolean {
            if (params.size != args.size) return false
            for (i in params.indices) {
                if (params[i]!!.isPrimitive) {
                    if (params[i] == Int::class.javaPrimitiveType && args[i] !is Int) return false
                    else if (params[i] == Byte::class.javaPrimitiveType && args[i] !is Byte) return false
                    else if (params[i] == Char::class.javaPrimitiveType && args[i] !is Char) return false
                    else if (params[i] == Boolean::class.javaPrimitiveType && args[i] !is Boolean) return false
                    else if (params[i] == Double::class.javaPrimitiveType && args[i] !is Double) return false
                    else if (params[i] == Float::class.javaPrimitiveType && args[i] !is Float) return false
                    else if (params[i] == Long::class.javaPrimitiveType && args[i] !is Long) return false
                    else if (params[i] == Short::class.javaPrimitiveType && args[i] !is Short) return false
                } else if (args[i] != null && !params[i]!!.isInstance(args[i])) return false
            }
            return true
        }
    }
}
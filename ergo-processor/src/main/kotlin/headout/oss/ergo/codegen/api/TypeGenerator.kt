package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.FileSpec

/**
 * Created by shivanshs9 on 13/08/20.
 */
interface TypeGenerator {
    fun brewKotlin(brewHook: (FileSpec.Builder) -> Unit = { }): FileSpec
}
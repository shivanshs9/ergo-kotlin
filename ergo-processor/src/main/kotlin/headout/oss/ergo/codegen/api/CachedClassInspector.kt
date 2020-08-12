package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import headout.oss.ergo.utils.kotlinMetadata
import javax.lang.model.element.TypeElement

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * This cached API over [ClassInspector] that caches certain lookups Moshi does potentially multiple
 * times. This is useful mostly because it avoids duplicate reloads in cases like common base
 * classes, common enclosing types, etc.
 */
@KotlinPoetMetadataPreview
internal class CachedClassInspector(private val classInspector: ClassInspector) {
    private val elementToSpecCache = mutableMapOf<TypeElement, TypeSpec>()
    private val kmClassToSpecCache = mutableMapOf<ImmutableKmClass, TypeSpec>()
    private val metadataToKmClassCache = mutableMapOf<Metadata, ImmutableKmClass>()

    fun toImmutableKmClass(metadata: Metadata): ImmutableKmClass {
        return metadataToKmClassCache.getOrPut(metadata) {
            metadata.toImmutableKmClass()
        }
    }

    fun toImmutableKmClass(element: TypeElement): ImmutableKmClass? =
        element.kotlinMetadata?.let { toImmutableKmClass(it) }

    fun toTypeSpec(kmClass: ImmutableKmClass): TypeSpec {
        return kmClassToSpecCache.getOrPut(kmClass) {
            kmClass.toTypeSpec(classInspector)
        }
    }
}

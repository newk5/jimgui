@file:Suppress("unused")

package org.ice1000.gradle

import org.intellij.lang.annotations.Language

/**
 * @author ice1000
 */
open class GenDrawListTask : GenTask("JImGuiDrawListGen", "imgui_draw_list") {
	init {
		description = "Generate binding for ImGui::GetDrawList"
	}

	@Language("JAVA", prefix = "class A{", suffix = "}")
	override val userCode = """/** subclass-private by design */
	protected long nativeObjectPtr;

	/** package-private by design */
	JImGuiDrawListGen(long nativeObjectPtr) {
		this.nativeObjectPtr = nativeObjectPtr;
	}
"""

	override fun java(javaCode: StringBuilder) {
		primitiveMembers.forEach { (type, name, annotation) ->
			javaCode.genJavaObjectiveMemberAccessor(name, annotation, type)
		}
		functions.forEach { genJavaFun(javaCode, it) }
	}

	override fun `c++`(cppCode: StringBuilder) {
		primitiveMembers.joinLinesTo(cppCode) { (type, name) -> `c++PrimitiveAccessor`(type, name, ", jlong nativeObjectPtr") }
		functions.forEach { (name, type, params) -> `genC++Fun`(params.dropLast(1), name, type, cppCode, ", jlong nativeObjectPtr") }
	}

	override val `c++Expr` = "(reinterpret_cast<ImDrawList *> (nativeObjectPtr))->"
	private val functions = listOf(
			Fun("pushClipRect",
					size("clipRectMin"),
					size("clipRectMax"),
					bool("intersectWithCurrentClipRect", default = false),
					nativeObjectPtr),
			Fun.private("pushClipRectFullScreen", nativeObjectPtr),
			Fun.private("popClipRect", nativeObjectPtr),
			Fun.private("popTextureID", nativeObjectPtr),

			// Primitives
			Fun.private("addLine", pos("a"), pos("b"), u32, thickness, nativeObjectPtr),
			Fun.private("addRect", pos("a"), pos("b"), u32, rounding, roundingFlags, thickness, nativeObjectPtr),
			Fun.private("addRectFilled", pos("a"), pos("b"), u32, rounding, roundingFlags, nativeObjectPtr),
			Fun.private("addRectFilledMultiColor", pos("a"), pos("b"),
					int("colorUpperLeft"), int("colorUpperRight"),
					int("colorBottomRight"), int("colorBottomLeft"),
					nativeObjectPtr),
			Fun.private("addQuad", pos("a"), pos("b"), pos("c"), pos("d"), u32, thickness, nativeObjectPtr),
			Fun.private("addQuadFilled", pos("a"), pos("b"), pos("c"), pos("d"), u32, nativeObjectPtr),
			Fun.private("addTriangle", pos("a"), pos("b"), pos("c"), u32, thickness, nativeObjectPtr),
			Fun.private("addTriangleFilled", pos("a"), pos("b"), pos("c"), u32, nativeObjectPtr),
			Fun.private("addCircle", pos("centre"), float("radius"), u32, numSegments, thickness, nativeObjectPtr),
			Fun.private("addCircleFilled", pos("centre"), float("radius"), u32, numSegments, nativeObjectPtr),

			// Stateful path API, add points then finish with PathFillConvex() or PathStroke()
			Fun.private("pathClear", nativeObjectPtr),
			Fun.private("pathLineTo", pos(), nativeObjectPtr),
			Fun.private("pathLineToMergeDuplicate", pos(), nativeObjectPtr)
	)

	private val primitiveMembers = listOf(PPT("int", "Flags",
			annotation = "@MagicConstant(flagsFromClass = JImDrawListFlags.class)"))
}

package com.qlive.qnlivekit.demo.fbo

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


val vs = """#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 aTextureCoord;
uniform mat4 uPositionMatrix;
uniform mat4 uTextureMatrix;
out vec2 vTexCoord;
void main() { 
     gl_Position  =  (uPositionMatrix * vPosition);
     vTexCoord =  (uTextureMatrix * aTextureCoord).xy;
}"""


val fs = """#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
in vec2 vTexCoord;
out vec4 vFragColor;
void main() {
     vFragColor = texture(uTextureUnit,vTexCoord);
}"""

/**
 * 顶点坐标
 * (x,y,z)
 */
val POSITION_VERTEX = floatArrayOf(
    0f, 0f, 0f,  //顶点坐标V0
    1f, 1f, 0f,  //顶点坐标V1
    -1f, 1f, 0f,  //顶点坐标V2
    -1f, -1f, 0f,  //顶点坐标V3
    1f, -1f, 0f //顶点坐标V4
)

val TEX_VERTEX = floatArrayOf(
    0.5f, 0.5f,  //纹理坐标V0
    1f, 1f,  //纹理坐标V1
    0f, 1f,  //纹理坐标V2
    0f, 0f,  //纹理坐标V3
    1f, 0f //纹理坐标V4
)

val VERTEX_INDEX = shortArrayOf(
    0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
    0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
    0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
    0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
)

class TextureRenderer {

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    private val mTexVertexBuffer: FloatBuffer
    private val mVertexIndexBuffer: ShortBuffer
    private var mProgram = 0
    private var textureId = 0
    private var textureId2 = 0
    private var uTextureUnitLocation = 0
    private var uTextureMatrixLocation = 0
    private var uPositionMatrixLocation = 0
    private val mOpenGLTools = OpenGLTools()

    // private val mTextureScaler by lazy { TextureScaler() }
    var w = 0
        private set
    var h = 0
        private set

    init {
        //分配内存空间,每个浮点型占4字节空间
        //传入指定的坐标数据
        vertexBuffer.put(POSITION_VERTEX)
        vertexBuffer.position(0)

        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(TEX_VERTEX)
        mTexVertexBuffer.position(0)
        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(VERTEX_INDEX)
        mVertexIndexBuffer.position(0)

        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        //编译
        val vertexShaderId = ShaderUtils.compileVertexShader(vs)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fs)
        //链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)

        uPositionMatrixLocation = GLES30.glGetUniformLocation(mProgram, "uPositionMatrix")
        uTextureMatrixLocation = GLES30.glGetUniformLocation(mProgram, "uTextureMatrix")
        uTextureUnitLocation = GLES30.glGetUniformLocation(mProgram, "uTextureUnit")

        Log.d(
            "mjl",
            "  TextureRenderer $vertexShaderId $fragmentShaderId $uTextureUnitLocation $uPositionMatrixLocation "
        )
    }

    private var isAttach = false
    fun attach(width: Int, height: Int) {
        Log.d("onSurfaceChanged", "onSurfaceChanged $width  $height")
        mOpenGLTools.createFBOTexture(width, height)
        mOpenGLTools.createFrameBuffer()
        GLES30.glViewport(0, 0, width, height)
        isAttach = true
        w = width
        h = height
    }

    private var targetW = 0
    private var targetH = 0

    private var targetX = 0
    private var targetY = 0

    fun setTargetSize(width: Int, height: Int, x: Int, y: Int) {
        targetW = width
        targetH = height
        targetX = x
        targetY = y
    }

    fun drawFrame(textureIdb: Int, textureIdf: Int, f16Matrix: FloatArray, rotation: Int): Int {

        textureId = textureIdb
        textureId2 = textureIdf

        mOpenGLTools.bindFBO()
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        //使用程序片段
        GLES30.glUseProgram(mProgram)
        drawPic1(f16Matrix, rotation.toFloat())
        drawPic2(textureId2, f16Matrix, rotation.toFloat())
        Log.d("drawPic1", "drawPic1")
        Log.d("onSurfaceChanged", "onReadPixel $w  $h")
        mOpenGLTools.unbindFBO()
        return mOpenGLTools.textures!![0]
    }

    private fun drawPic1(f16Matrix: FloatArray, rotationDegrees: Float) {
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(uTextureUnitLocation, 0)

        GLES30.glUniform1i(uPositionMatrixLocation, 0)

        val origin = f16Matrix.clone()
        Matrix.setRotateM(origin, 0, 360 - rotationDegrees, 0f, 0f, 1.0f)
        if (rotationDegrees == 270F) {
            Matrix.scaleM(origin, 0, -1f, 1f, 1f)
        }
        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(
            uPositionMatrixLocation,
            1,
            false,
            origin,
            0
        )

        val rotationMatrix = FloatArray(16)
        Matrix.setIdentityM(rotationMatrix, 0)
        GLES30.glUniform1i(uTextureMatrixLocation, 0)
        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(
            uTextureMatrixLocation,
            1,
            false,
            rotationMatrix,
            0
        )
        // 绘制
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES20.GL_UNSIGNED_SHORT,
            mVertexIndexBuffer
        )
    }

    private fun drawPic2(texture: Int, f16Matrix: FloatArray, rotationDegrees: Float) {

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)

        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glUniform1i(uTextureUnitLocation, 0)

        GLES30.glUniform1i(uPositionMatrixLocation, 0)

        val matrix = FloatArray(16)
        Matrix.setIdentityM(matrix, 0)

        val x = (targetX.toFloat() + targetW / 2 - h / 2) / (h / 2)
        val y = -(targetY.toFloat() + targetH / 2 - w / 2) / (w / 2)

        val v = floatArrayOf(x, y, 0.0f, 0f)
        val rotationMatrix = f16Matrix.clone()

        Matrix.rotateM(
            rotationMatrix, 0, rotationDegrees, 0.0f, 0.0f, 1.0f
        )
        val rotatedV = FloatArray(4)
        Matrix.multiplyMV(rotatedV, 0, rotationMatrix, 0, v, 0)
        Matrix.translateM(matrix, 0, rotatedV[0], rotatedV[1], 1f)

        if (targetW <= 0) {
            targetW = w
        }
        if (targetH == 0) {
            targetH = h
        }
        val scaleX = targetW / w.toFloat()
        val scaleY = targetH / h.toFloat()

        Matrix.scaleM(matrix, 0, scaleX, scaleY, 1f)

        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(
            uPositionMatrixLocation,
            1,
            false,
            matrix,
            0
        )



        var sy = 0f
        var sx = 0f
        val realw = h.toFloat() * (targetW / targetH)
        val realh = w.toFloat() / (targetW / targetH)
        if (realw < w) {
            sy = 1f
            sx = realw / w
        } else {
            sy = realh / h
            sx = 1f
        }

        val matrix2 = FloatArray(16)
        Matrix.setIdentityM(matrix2, 0)
        Matrix.scaleM(matrix2, 0, sx, sy, 1f)

        GLES30.glUniform1i(uTextureMatrixLocation, 0)
        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(
            uTextureMatrixLocation,
            1,
            false,
            matrix2,
            0
        )

        // 绘制
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES20.glDrawElements(
            GLES30.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES30.GL_UNSIGNED_SHORT,
            mVertexIndexBuffer
        )
    }

    fun detach() {
        if (isAttach) {
            mOpenGLTools.unbindFBO()
        }
        isAttach = false
    }
}
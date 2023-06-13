package com.qlive.qnlivekit.demo.fbo

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import com.qlive.qnlivekit.demo.fbo.OpenGLTools.bindFBO
import com.qlive.qnlivekit.demo.fbo.OpenGLTools.createFBOTexture
import com.qlive.qnlivekit.demo.fbo.OpenGLTools.createFrameBuffer
import com.qlive.qnlivekit.demo.fbo.OpenGLTools.textures
import com.qlive.qnlivekit.demo.fbo.OpenGLTools.unbindFBO
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


private val vs = """#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 aTextureCoord;
uniform mat4 uPositionMatrix;
out vec2 vTexCoord;
void main() { 
     gl_Position  =  (uPositionMatrix * vPosition);
     vTexCoord =  aTextureCoord.xy;
}"""


private val fs = """#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
in vec2 vTexCoord;
out vec4 vFragColor;
void main() {
     vFragColor = texture(uTextureUnit,vTexCoord);
}"""

class TextureRenderer {

    private val vertexBuffer: FloatBuffer

    private val mTexVertexBuffer: FloatBuffer
    private val mVertexIndexBuffer: ShortBuffer
    private var mProgram = 0
    private var textureId = 0
    private var textureId2 = 0
    private var uTextureUnitLocation = 0
    private var uPositionMatrixLocation = 0


    var w = 0
        private set
    var h = 0
        private set

    /**
     * 顶点坐标
     * (x,y,z)
     */
    private val POSITION_VERTEX = floatArrayOf(
        0f, 0f, 0f,  //顶点坐标V0
        1f, 1f, 0f,  //顶点坐标V1
        -1f, 1f, 0f,  //顶点坐标V2
        -1f, -1f, 0f,  //顶点坐标V3
        1f, -1f, 0f //顶点坐标V4
    )

    /**
     * 纹理坐标
     * (s,t)
     */
//    private val TEX_VERTEX = floatArrayOf(
//        0.5f, 0.5f,  //纹理坐标V0
//        1f, 0f,  //纹理坐标V1
//        0f, 0f,  //纹理坐标V2
//        0f, 1.0f,  //纹理坐标V3
//        1f, 1.0f //纹理坐标V4
//    )

    private val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f,  //纹理坐标V0
        1f, 1f,  //纹理坐标V1
        0f, 1f,  //纹理坐标V2
        0f, 0f,  //纹理坐标V3
        1f, 0f //纹理坐标V4
    )

    /**
     * 索引
     */
    private val VERTEX_INDEX = shortArrayOf(
        0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
    )

    init {
        //分配内存空间,每个浮点型占4字节空间
        vertexBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
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
        uTextureUnitLocation = GLES30.glGetUniformLocation(mProgram, "uTextureUnit")

        Log.d(
            "mjl",
            "  TextureRenderer $vertexShaderId $fragmentShaderId $uTextureUnitLocation $uPositionMatrixLocation "
        )
    }

    private var isAttach = false
    fun attach(width: Int, height: Int) {
        Log.d("onSurfaceChanged", "onSurfaceChanged $width  $height")
        createFBOTexture(width, height)
        createFrameBuffer()
        GLES30.glViewport(0, 0, width, height)
        isAttach = true
        w = width
        h = height
    }

    var targetW = 460
    var targetH = 320

    var targetX = 0
    var targetY = 800
    fun drawFrame(textureIdb: Int, textureIdf: Int, f16Matrix: FloatArray, rotation: Int): Int {
        textureId = textureIdb
        textureId2 = textureIdf
        Log.d("onDrawFrame", "onDrawFrame")
        bindFBO()
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        //使用程序片段
        GLES30.glUseProgram(mProgram)
        drawPic1(f16Matrix, rotation.toFloat())
        drawPic2(f16Matrix, rotation.toFloat())
        Log.d("drawPic1", "drawPic1")
        Log.d("onSurfaceChanged", "onReadPixel $w  $h")
        unbindFBO()
        return textures!![0]
        //OpenGLTools.INSTANCE.deleteFBO();
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
        // 绘制
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES20.GL_UNSIGNED_SHORT,
            mVertexIndexBuffer
        )
    }

    private fun drawPic2(f16Matrix: FloatArray, rotationDegrees: Float) {
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId2)
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
        Log.d("mjl", "  multiplyMV $rotatedV")

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
            OpenGLTools.unbindFBO()
        }
        isAttach = false
    }
}
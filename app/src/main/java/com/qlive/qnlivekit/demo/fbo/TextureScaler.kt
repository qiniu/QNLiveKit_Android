package com.qlive.qnlivekit.demo.fbo

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class TextureScaler {

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    private val mTexVertexBuffer: FloatBuffer
    private val mVertexIndexBuffer: ShortBuffer
    private var mProgram = 0
    private var uTextureUnitLocation = 0

    private var uPositionMatrixLocation = 0
    private val mOpenGLTools = OpenGLTools()
    var outW = 0
        private set
    var outH = 0
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
        uTextureUnitLocation = GLES30.glGetUniformLocation(mProgram, "uTextureUnit")

        Log.d(
            "mjl",
            "  TextureRenderer $vertexShaderId $fragmentShaderId $uTextureUnitLocation $uPositionMatrixLocation "
        )
    }

    private var isAttach = false
    fun setOutPutSize(width: Int, height: Int) {
        outW = width
        outH = height
    }

    private var inputW = 0;
    private var inputH = 0;

    private var sx = 1f
    private var sy = 1f
    fun setInputSize(width: Int, height: Int) {
        Log.d("onSurfaceChanged", "onSurfaceChanged $width  $height")
        mOpenGLTools.createFBOTexture(width, height)
        mOpenGLTools.createFrameBuffer()
        GLES30.glViewport(0, 0, width, height)
        isAttach = true

        this.inputW = width
        this.inputH = height;

        if (inputW.toFloat() / outW > inputH.toFloat() / outH) {
            sy = 1f
            sx = outH.toFloat() * inputW / inputH / outW
        } else {
            sx = 1f
            sy = outW.toFloat() * inputH / inputW / outH
        }
    }

    fun draw(textureId: Int, f16Matrix: FloatArray, rotationDegrees: Int): Int {
        mOpenGLTools.bindFBO()
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(uTextureUnitLocation, 0)

        GLES30.glUniform1i(uPositionMatrixLocation, 0)
        val matrix = f16Matrix.clone()
       // Matrix.setIdentityM(matrix, 0)
        Matrix.scaleM(matrix, 0, 1f, 1f, 1f)
        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(
            uPositionMatrixLocation,
            1,
            false,
            matrix,
            0
        )

        // 绘制
        GLES20.glDrawElements(
            GLES30.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES30.GL_UNSIGNED_SHORT,
            mVertexIndexBuffer
        )

        mOpenGLTools.unbindFBO()
        return mOpenGLTools.textures!![0]
    }

    fun detach() {
        if (isAttach) {
            mOpenGLTools.unbindFBO()
        }
        isAttach = false
    }

}
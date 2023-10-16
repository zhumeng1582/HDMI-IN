package com.camer.separate.filter;

import android.opengl.GLES11Ext;


import static android.opengl.GLES30.*;

import com.camer.separate.R;
import com.camer.separate.utils.GLUtil;

/**
 * Created by wangyt on 2019/5/24
 */
public class OesFilter extends BaseFilter{

    public OesFilter() {
        super();
    }

    @Override
    public int initProgram() {
        return GLUtil.createAndLinkProgram(R.raw.texture_vertex_shader, R.raw.texture_oes_fragtment_shader);
    }

    @Override
    public void bindTexture() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId()[0]);
        glUniform1i(hTexture, 0);
    }
}

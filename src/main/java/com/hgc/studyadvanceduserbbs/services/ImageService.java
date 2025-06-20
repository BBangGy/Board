package com.hgc.studyadvanceduserbbs.services;

import com.hgc.studyadvanceduserbbs.entities.ImageEntity;
import com.hgc.studyadvanceduserbbs.entities.UserEntity;
import com.hgc.studyadvanceduserbbs.mappers.ImageMapper;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ImageService {
    private final ImageMapper imageMapper;

    @Autowired
    public ImageService(ImageMapper imageMapper) {
        this.imageMapper = imageMapper;
    }

    public CommonResult add(UserEntity user, ImageEntity image) {
        if (user == null ||
                user.isDeleted() || user.isSuspended()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        if (image == null ||
                image.getName() == null ||
                image.getData() == null ||
                image.getData().length == 0 ||
                image.getContentType() == null) {
            return CommonResult.FAILURE;
        }
        image.setCreatedAt(LocalDateTime.now());
        return this.imageMapper.insert(image)>0?CommonResult.SUCCESS:CommonResult.FAILURE;
    }

    public ImageEntity getById(int id) {
        if (id < 1) {
            return null;
        }
        return this.imageMapper.selectById(id);
    }
}

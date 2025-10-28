package com.imperialgrand.backend.Menu.MenuItem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.Menu.Exception.MenuItemNotFoundExcetion;
import com.imperialgrand.backend.Menu.entities.MenuItem;
import com.imperialgrand.backend.Menu.repo.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public Page<MenuItem> menuItemLists(Long categoryId, Long subcategoryId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        Page<MenuItem> menuItemResult = null;
        String menuItemRedisKey = "";

        if(subcategoryId != null) {
            menuItemRedisKey = CatAndSubKey(categoryId, subcategoryId, page, size);    //Ig:Cat-1:Sub-2:Page-1:Size-1
            String menuItemCached = getRedisString(menuItemRedisKey);

            if(menuItemCached != null){
                menuItemResult = convertStringToObject(menuItemCached);
            }else{
                System.out.println("CACHE MISS! Fetching Cat and Sub from database");
                menuItemResult = menuItemRepository.findByCategoryIdAndSubcategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId, subcategoryId, pageable);
            }

        }else{
            menuItemRedisKey = CatKey(categoryId, page, size);
            String menuItemCached = getRedisString(menuItemRedisKey);

            if(menuItemCached != null){

            }else{
                menuItemRepository.findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId, pageable);
            }


        }
        return menuItemResult;
    }



    private Page<MenuItem> convertStringToObject(String cached){
        Page<MenuItem> redisResult = null;
        try{
            System.out.println("CACHE HIT! Fetching Cat and Sub from redis");
            redisResult =  mapper.readValue(cached, new TypeReference<Page<MenuItem>>() {});
        }catch(JsonProcessingException e){
            System.out.println("Menu Item: Failed to fetch from redis: " + e.getMessage());
        }
        return redisResult;
    }

    private String getRedisString(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public MenuItem getMenuItemById(Long id){
       return menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundExcetion("Menu Item Not Found"));
    }



    private String CatAndSubKey(Long categoryId, Long subcategoryId, int page, int size){
        return "ig:Cat-" + categoryId + ":Sub-" + subcategoryId + ":Page-" + page + ":Size-" + size;
    }

    private String CatKey(Long categoryId, int page, int size){
        return "ig:Cat-" + categoryId + ":Page-" + page + ":Size-" + size;
    }


}

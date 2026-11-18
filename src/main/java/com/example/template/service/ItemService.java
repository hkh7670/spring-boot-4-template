package com.example.template.service;

import com.example.template.domain.Item;
import com.example.template.enums.ResponseCodeEnum;
import com.example.template.exception.ApiErrorException;
import com.example.template.repository.ItemQueryRepository;
import com.example.template.repository.ItemRepository;
import com.example.template.service.dto.ItemCommand;
import com.example.template.service.dto.ItemResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

  private final ItemRepository itemRepository;
  private final ItemQueryRepository itemQueryRepository;

  public List<ItemResult> findAll() {
    return itemQueryRepository.findAllProjected();
  }

  public ItemResult findById(Long id) {
    Item item =
        itemRepository
            .findById(id)
            .orElseThrow(() -> new ApiErrorException(ResponseCodeEnum.NOT_FOUND));
    return ItemResult.from(item);
  }

  @Transactional
  public ItemResult create(ItemCommand command) {
    Item item = Item.of(command.name(), command.age(), command.description());
    Item saved = itemRepository.save(item);
    return ItemResult.from(saved);
  }

  @Transactional
  public ItemResult update(Long id, ItemCommand command) {
    Item item =
        itemRepository
            .findById(id)
            .orElseThrow(() -> new ApiErrorException(ResponseCodeEnum.NOT_FOUND));
    item.updateName(command.name());
    item.updateAge(command.age());
    item.updateDescription(command.description());
    return ItemResult.from(item);
  }

  @Transactional
  public void delete(Long id) {
    if (!itemRepository.existsById(id)) {
      throw new ApiErrorException(ResponseCodeEnum.NOT_FOUND);
    }
    itemRepository.deleteById(id);
  }
}

package pl.code.house.makro.mapa.auth.domain.product;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.code.house.makro.mapa.auth.domain.user.dto.ProductDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFacade {

  private final ProductRepository productRepository;

  public List<ProductDto> findAll() {
    return productRepository.findAll()
        .stream()
        .map(Product::toDto)
        .collect(toList());
  }

  public Optional<ProductDto> findById(Long productId) {
    return productRepository.findById(productId)
        .map(Product::toDto);
  }
}
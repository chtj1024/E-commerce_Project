package com.taejun.shop.domain.cart.service;


import com.taejun.shop.domain.cart.dto.CartItemAddRequest;
import com.taejun.shop.domain.cart.dto.CartItemResponse;
import com.taejun.shop.domain.cart.dto.CartQuantityUpdateRequest;
import com.taejun.shop.domain.cart.entity.CartItem;
import com.taejun.shop.domain.cart.repository.CartItemRepository;
import com.taejun.shop.domain.member.entity.Member;
import com.taejun.shop.domain.member.repository.MemberRepository;
import com.taejun.shop.domain.product.entity.Product;
import com.taejun.shop.domain.product.enums.ProductStatus;
import com.taejun.shop.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public List<CartItemResponse> findAll(String email) {
        Member member = getMember(email);

        return cartItemRepository
                .findAllByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(CartItemResponse::from)
                .toList();

    }

    @Transactional
    public CartItemResponse add(
            String email,
            CartItemAddRequest request
    ) {
        Member member = getMember(email);

        Product product = productRepository
                .findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다."
                ));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "현재 판매 중인 상품만 담을 수 있습니다."
            );
        }

        CartItem cartItem = cartItemRepository
                .findByMemberIdAndProductId(
                        member.getId(),
                        product.getId()
                )
                .orElseGet(() -> new CartItem( // orElseGet() : 앞에서 받은 Optional의 값이 없으면 이 코드 실행
                        member,
                        product,
                        0
                ));

        int newQuantity = cartItem.getQuantity() + request.quantity();

        validateStock(product, newQuantity);

        cartItem.changeQuantity(newQuantity);

        return CartItemResponse.from(
                cartItemRepository.save(cartItem)
        );
    }

    @Transactional
    public CartItemResponse updateQuantity(
            String email,
            Long cartItemId,
            CartQuantityUpdateRequest request
    ) {
        Member member = getMember(email);

        CartItem cartItem = getCartItem(
                cartItemId,
                member.getId()
        );

        validateStock(
                cartItem.getProduct(),
                request.quantity()
        );

        cartItem.changeQuantity(request.quantity());

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void delete(
            String email,
            Long cartItemId
    ) {
        Member member = getMember(email);

        CartItem cartItem = getCartItem(
                cartItemId,
                member.getId()
        );

        cartItemRepository.delete(cartItem);
    }

    private Member getMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "로그인이 필요합니다."
                ));
    }

    private CartItem getCartItem(
            Long cartItemId,
            Long memberId
    ) {
        return cartItemRepository
                .findByIdAndMemberId(cartItemId, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "장바구니 상품을 찾을 수 없습니다."
                ));
    }

    private void validateStock(
            Product product,
            int quantity
    ) {
        if (quantity > product.getStockQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "상품 재고보다 많이 담을 수 없습니다."
            );
        }
    }
}

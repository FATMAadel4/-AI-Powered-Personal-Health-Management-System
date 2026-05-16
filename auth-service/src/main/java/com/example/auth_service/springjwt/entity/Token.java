package com.example.auth_service.springjwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data       // equal to @Setter , @Getter , @ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jwt")
public class Token
{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  public String token;

  @Enumerated(EnumType.STRING)
  public TokenType tokenType = TokenType.BEARER;

  public boolean revoked;

  public boolean expired;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  public User user;
}

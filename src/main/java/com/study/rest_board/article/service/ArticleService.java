package com.study.rest_board.article.service;

import com.study.rest_board.article.domain.ArticleComment;
import com.study.rest_board.article.dto.reqdto.*;
import com.study.rest_board.article.dto.resdto.ArticleCommentResDto;
import com.study.rest_board.article.dto.resdto.ArticleResDto;
import com.study.rest_board.article.domain.Article;
import com.study.rest_board.article.exception.ArticleErrorCode;
import com.study.rest_board.article.repository.ArticleRepository;
import com.study.rest_board.article.repository.CommentRepository;
import com.study.rest_board.common.exception.GlobalBusinessException;
import com.study.rest_board.user.domain.User;
import com.study.rest_board.user.exception.UserErrorCode;
import com.study.rest_board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;

	@Transactional
	public ArticleResDto saveArticle(ArticleSaveReqDto reqDto) {
		Article savedArticle = articleRepository.save(reqDto.toEntity());
		return ArticleResDto.from(savedArticle);
	}

	public ArticleResDto findArticleById(long id) {
		Article findArticle = articleRepository.findById(id).orElseThrow(() -> new GlobalBusinessException(ArticleErrorCode.ARTICLE_NOT_FOUND));
		return ArticleResDto.from(findArticle);
	}

	@Transactional
	public ArticleResDto updateArticleById(long id, ArticleUpdateReqDto reqDto) {

		Article article = articleRepository.findById(id).orElseThrow(() -> new GlobalBusinessException(ArticleErrorCode.ARTICLE_NOT_FOUND));
		if (!article.isEqualPassword(reqDto.getPassword())) {
			throw new GlobalBusinessException(ArticleErrorCode.INVALID_PASSWORD);
		}

		article.update(reqDto);
		return ArticleResDto.from(article);
	}

	public List<ArticleResDto> findAllArticles() {
		List<Article> articles = articleRepository.findAll();

		return articles.stream().map(ArticleResDto::from).toList();
	}

	@Transactional
	public void deleteArticleById(long id, PasswordReqDto authDto) {

		Article article = articleRepository.findById(id).orElseThrow(() -> new GlobalBusinessException(ArticleErrorCode.ARTICLE_NOT_FOUND));

		if (!article.isEqualPassword(authDto.getPassword())) {
			throw new GlobalBusinessException(ArticleErrorCode.INVALID_PASSWORD);
		}

		articleRepository.deleteById(article.getId());
	}

	@Transactional
	public ArticleCommentResDto saveComment(ArticleCommentSaveReqDto reqDto) {

		Article findArticle = articleRepository.findById(reqDto.getArticleId()).orElseThrow(() -> new GlobalBusinessException(ArticleErrorCode.ARTICLE_NOT_FOUND));

		User findUser = userRepository.findById(reqDto.getUserId()).orElseThrow(() -> new GlobalBusinessException(UserErrorCode.USER_NOT_FOUND));

		ArticleComment beforeSaveComment = reqDto.toEntity(findArticle, findUser);

		ArticleComment savedComment = commentRepository.save(beforeSaveComment);

		return ArticleCommentResDto.from(savedComment);
	}

	@Transactional
	public ArticleCommentResDto updateComment(long id,ArticleCommentUpdateReqDto reqDto) {

		ArticleComment findComment = commentRepository.findById(id).orElseThrow(() -> new GlobalBusinessException(ArticleErrorCode.COMMENT_NOT_FOUND));

		if(!findComment.isSameWriter(reqDto.getUserId())) {
			throw new GlobalBusinessException(ArticleErrorCode.INVALID_WRITER);
		}

		findComment.updateComment(reqDto.getContent());

		return ArticleCommentResDto.from(findComment);
	}

	@Transactional
	public String deleteCommentById(long commentId, long userId) {

		ArticleComment findComment = commentRepository.findById(commentId).orElseThrow(() -> new GlobalBusinessException(ArticleErrorCode.COMMENT_NOT_FOUND));

		if(!findComment.isSameWriter(userId)) {
			throw new GlobalBusinessException(ArticleErrorCode.INVALID_WRITER);
		}

		commentRepository.deleteById(commentId);
		return "delete ok";

	}
}

package com.sgp.controller.licitacao;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice(assignableTypes={LicitacaoController.class,PortfolioController.class})
public class LicitacaoExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class})
    public String regra(RuntimeException e,HttpServletRequest request,RedirectAttributes ra){ra.addFlashAttribute("erro",e.getMessage());String ref=request.getHeader("Referer");return "redirect:"+(ref!=null&&ref.startsWith(request.getScheme()+"://"+request.getServerName())?ref:"/licitacoes");}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String duplicado(DataIntegrityViolationException e,HttpServletRequest request,RedirectAttributes ra){ra.addFlashAttribute("erro","Registro duplicado ou relacionado a dados existentes. Confira os números dos itens.");String ref=request.getHeader("Referer");return "redirect:"+(ref!=null&&ref.startsWith(request.getScheme()+"://"+request.getServerName())?ref:"/licitacoes");}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tamanho(MaxUploadSizeExceededException e,HttpServletRequest request,RedirectAttributes ra){ra.addFlashAttribute("erro","O arquivo excede o limite de 20 MB.");String ref=request.getHeader("Referer");return "redirect:"+(ref!=null&&ref.startsWith(request.getScheme()+"://"+request.getServerName())?ref:"/licitacoes");}
}

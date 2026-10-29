package com.sgp.controller;

import com.sgp.model.GrupoProduto;
import com.sgp.model.Produto;
import com.sgp.repository.ProcessoProdutoRepository;
import com.sgp.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProcessoProdutoRepository processoProdutoRepository;

    @GetMapping("/cadastrar")
    public String showCreateForm(Model model) {
        model.addAttribute("grupos", GrupoProduto.values());
        model.addAttribute("produto", new Produto());
        return "produtos/cadastrar-produto";
    }

    @PostMapping("/cadastrar")
    public String create(@ModelAttribute Produto produto,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataCadastro) {
        produto.setDataCadastro(dataCadastro != null ? dataCadastro : LocalDate.now());
        produtoRepository.save(produto);
        return "redirect:/produtos/listar";
    }

    @GetMapping("/listar")
    public String list(Model model) {
        List<Produto> produtos = produtoRepository.findAll();
        model.addAttribute("produtos", produtos);
        model.addAttribute("vinculosProcessos", processoProdutoRepository.contarProcessosPorProduto().stream()
                .collect(Collectors.toMap(item -> item.getId(), item -> item.getTotal())));
        return "produtos/listar-produtos";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Produto produto = produtoRepository.findById(id).orElse(null);
        if (produto == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Produto não encontrado.");
            return "redirect:/produtos/listar";
        }

        long vinculos = processoProdutoRepository.contarProcessosPorProduto(id);
        if (vinculos > 0) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "O produto " + produto.getNomeItem() + " está vinculado a " + vinculos + " processo"
                            + (vinculos == 1 ? "" : "s") + " e não pode ser excluído.");
            return "redirect:/produtos/listar";
        }

        try {
            produtoRepository.delete(produto);
            produtoRepository.flush();
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto excluído com sucesso.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Não foi possível excluir o produto porque ele possui vínculos no sistema.");
        }
        return "redirect:/produtos/listar";
    }

    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Produto produto = produtoRepository.findById(id).orElseThrow();
        model.addAttribute("grupos", GrupoProduto.values());
        model.addAttribute("produto", produto);
        return "produtos/editar-produto";
    }

    @PostMapping("/editar/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Produto produtoForm,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataCadastro) {
        Produto produto = produtoRepository.findById(id).orElseThrow();
        produto.setCodigo(produtoForm.getCodigo());
        produto.setNomeItem(produtoForm.getNomeItem());
        produto.setUnidadeMedida(produtoForm.getUnidadeMedida());
        produto.setSiglaUnidade(produtoForm.getSiglaUnidade());
        produto.setGrupo(produtoForm.getGrupo());
        produto.setDataCadastro(dataCadastro != null ? dataCadastro : produto.getDataCadastro());
        produto.setEspecificacoes(produtoForm.getEspecificacoes());
        produto.setFabricante(produtoForm.getFabricante());
        produto.setModelo(produtoForm.getModelo());
        produtoRepository.save(produto);
        return "redirect:/produtos/listar";
    }
}

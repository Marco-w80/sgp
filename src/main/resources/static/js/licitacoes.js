(function(){
  const alertaBonito=(titulo,texto,icone='warning')=>{
    if(window.Swal)return window.Swal.fire({icon:icone,title:titulo,text:texto,confirmButtonText:'Entendi',confirmButtonColor:'#166298'});
    window.alert(texto);return Promise.resolve();
  };
  const confirmarAcao=(titulo,texto,confirmarTexto='Confirmar')=>{
    if(window.Swal)return window.Swal.fire({icon:'warning',title:titulo,text:texto,showCancelButton:true,confirmButtonText:confirmarTexto,cancelButtonText:'Cancelar',confirmButtonColor:'#d33',cancelButtonColor:'#6c757d',reverseButtons:true}).then(resultado=>resultado.isConfirmed);
    return Promise.resolve(window.confirm(texto));
  };
  document.querySelectorAll('form[data-confirmacao]').forEach(formulario=>formulario.addEventListener('submit',async evento=>{
    evento.preventDefault();
    const confirmado=await confirmarAcao(formulario.dataset.confirmacaoTitulo||'Confirmar ação?',formulario.dataset.confirmacao,formulario.dataset.confirmacaoBotao||'Confirmar');
    if(confirmado)HTMLFormElement.prototype.submit.call(formulario);
  }));
  const botaoPncp=document.getElementById('buscar-dados-pncp'),mensagemPncp=document.getElementById('mensagem-pncp'),avisoDataPncp=document.getElementById('aviso-data-pncp'),carregandoPncp=document.getElementById('carregando-pncp'),previewItensPncp=document.getElementById('preview-itens-pncp'),corpoItensPncp=document.getElementById('corpo-itens-pncp'),totalItensPncp=document.getElementById('total-itens-pncp'),itensPncpJson=document.getElementById('itensPncpJson'),linkItensPncp=document.getElementById('linkItensPncp');
  if(botaoPncp&&mensagemPncp){
    const campo=id=>document.getElementById(id);
    const mostrar=(texto,tipo)=>{mensagemPncp.textContent=texto;mensagemPncp.className='alert mt-3 mb-0 alert-'+tipo};
    const resumoCarregamento=campo('resumo-carregamento-pncp'),avisoDemora=campo('aviso-demora-pncp');
    const iconesEtapa={pending:'fa-circle',active:'fa-spinner fa-spin',done:'fa-check',error:'fa-exclamation'};
    const atualizarEtapa=(nome,estado,descricao)=>{
      const etapa=document.querySelector('[data-pncp-step="'+nome+'"]');if(!etapa)return;
      etapa.className='pncp-loading-step pncp-step-'+estado;
      const icone=etapa.querySelector('.pncp-step-icon i'),detalhe=etapa.querySelector('small');
      if(icone)icone.className='fas '+iconesEtapa[estado];
      if(detalhe&&descricao)detalhe.textContent=descricao;
    };
    const prepararEtapas=()=>{
      atualizarEtapa('validacao','pending','Verificando o endereço informado');
      atualizarEtapa('dados','pending','Aguardando início');
      atualizarEtapa('itens','pending','Aguardando início');
      atualizarEtapa('formulario','pending','Aguardando dados');
      if(resumoCarregamento)resumoCarregamento.textContent='Preparando a consulta...';
      if(avisoDemora)avisoDemora.classList.add('d-none');
    };
    const linkPncpValido=link=>{try{const url=new URL(link);return url.protocol==='https:'&&url.hostname==='pncp.gov.br'&&/^\/app\/editais\/\d{14}\/\d{4}\/\d+\/?$/.test(url.pathname)}catch(ignorado){return false}};
    const consultarPncp=async(endpoint,link)=>{
      const headers={'Content-Type':'application/json'};
      const token=document.querySelector('meta[name="_csrf"]'),header=document.querySelector('meta[name="_csrf_header"]');
      if(token&&header)headers[header.content]=token.content;
      const respostaHttp=await fetch(endpoint,{method:'POST',headers,body:JSON.stringify({link})});
      let resposta={};try{resposta=await respostaHttp.json()}catch(ignorado){}
      if(!respostaHttp.ok)throw new Error(resposta.error||'O PNCP não respondeu corretamente.');
      return resposta;
    };
    const resumirErro=(tipo,erro)=>{
      const texto=erro&&erro.message?erro.message:'';
      if(/prazo|timeout|não respondeu/i.test(texto))return tipo==='dados'?'Dados gerais não responderam no prazo.':'Itens não responderam no prazo.';
      if(/localizar|não encontrad/i.test(texto))return 'Licitação não encontrada no PNCP.';
      return tipo==='dados'?'Falha ao consultar os dados gerais.':'Falha ao consultar os itens.';
    };
    const limparItens=()=>{if(itensPncpJson)itensPncpJson.value='';if(linkItensPncp)linkItensPncp.value='';if(corpoItensPncp)corpoItensPncp.textContent='';if(totalItensPncp)totalItensPncp.textContent='0 itens';if(previewItensPncp)previewItensPncp.classList.add('d-none')};
    const exibirItens=itens=>{
      if(!previewItensPncp||!corpoItensPncp||!itens.length)return;
      const formatar=valor=>valor===null||valor===undefined?'—':new Intl.NumberFormat('pt-BR',{maximumFractionDigits:4}).format(valor);
      const fragmento=document.createDocumentFragment();
      itens.forEach(item=>{
        const linha=document.createElement('tr');
        [item.numeroItem,item.descricao,formatar(item.quantidade),item.unidade,formatar(item.valorReferencia)].forEach(valor=>{const celula=document.createElement('td');celula.textContent=valor===null||valor===undefined?'':String(valor);linha.appendChild(celula)});
        fragmento.appendChild(linha);
      });
      corpoItensPncp.appendChild(fragmento);if(totalItensPncp)totalItensPncp.textContent=itens.length+' itens';previewItensPncp.classList.remove('d-none');
    };
    const entradaLink=campo('linkEdital');
    if(entradaLink)entradaLink.addEventListener('input',()=>{if(linkItensPncp&&linkItensPncp.value&&entradaLink.value.trim()!==linkItensPncp.value)limparItens()});
    botaoPncp.addEventListener('click',async()=>{
      const link=campo('linkEdital').value.trim();
      if(!linkPncpValido(link)){mostrar('Informe um link válido do PNCP.','danger');avisoDataPncp.classList.add('d-none');return}
      const rotulo=botaoPncp.querySelector('span'),rotuloOriginal=rotulo.textContent;
      botaoPncp.disabled=true;rotulo.textContent='Buscando...';mensagemPncp.classList.add('d-none');avisoDataPncp.classList.add('d-none');
      prepararEtapas();if(carregandoPncp)carregandoPncp.classList.remove('d-none');
      document.body.setAttribute('aria-busy','true');
      atualizarEtapa('validacao','active','Analisando o link do edital');
      atualizarEtapa('validacao','done','Link válido');
      atualizarEtapa('dados','active','Consultando os dados gerais');
      const podeImportarItens=Boolean(itensPncpJson&&linkItensPncp);
      atualizarEtapa('itens',podeImportarItens?'active':'done',podeImportarItens?'Consultando os itens em paralelo':'Não necessário nesta edição');
      if(resumoCarregamento)resumoCarregamento.textContent='Consultando dados gerais e itens em paralelo...';
      let houveErro=false;
      let dadosFinalizados=false,itensFinalizados=!podeImportarItens;
      const temporizadorDemora=window.setTimeout(()=>{
        if(avisoDemora)avisoDemora.classList.remove('d-none');
        if(resumoCarregamento)resumoCarregamento.textContent=dadosFinalizados&&!itensFinalizados?'Dados gerais recebidos. O PNCP ainda está enviando os itens...':'O PNCP está respondendo lentamente...';
      },5000);
      try{
        const consultas=[consultarPncp('/api/licitacoes/pncp/consultar/dados',link).then(valor=>{
          dadosFinalizados=true;atualizarEtapa('dados','done','Dados gerais recebidos');
          if(resumoCarregamento&&!itensFinalizados)resumoCarregamento.textContent='Dados gerais recebidos. Aguardando os itens...';
          return {tipo:'dados',valor};
        },erro=>{
          dadosFinalizados=true;atualizarEtapa('dados','error',resumirErro('dados',erro));return {tipo:'dados',erro};
        })];
        if(podeImportarItens)consultas.push(consultarPncp('/api/licitacoes/pncp/consultar/itens',link).then(valor=>{
          itensFinalizados=true;const quantidade=Array.isArray(valor.itens)?valor.itens.length:0;
          atualizarEtapa('itens','done',quantidade+' itens recebidos');
          if(resumoCarregamento&&!dadosFinalizados)resumoCarregamento.textContent='Itens recebidos. Aguardando os dados gerais...';
          return {tipo:'itens',valor};
        },erro=>{
          itensFinalizados=true;atualizarEtapa('itens','error',resumirErro('itens',erro));return {tipo:'itens',erro};
        }));
        const resultados=await Promise.all(consultas);
        const resultadoDados=resultados.find(resultado=>resultado.tipo==='dados');
        const resultadoItens=resultados.find(resultado=>resultado.tipo==='itens');
        const erros=[];
        const dadosCarregados=Boolean(resultadoDados&&!resultadoDados.erro);
        const itensCarregados=!podeImportarItens||Boolean(resultadoItens&&!resultadoItens.erro);
        const dados=dadosCarregados?(resultadoDados.valor.dados||{}):null;
        const itens=podeImportarItens&&itensCarregados&&Array.isArray(resultadoItens.valor.itens)?resultadoItens.valor.itens:[];

        if(dadosCarregados){
          atualizarEtapa('dados','done','Dados gerais recebidos');
        }else{
          houveErro=true;const erroDados=resumirErro('dados',resultadoDados&&resultadoDados.erro);
          erros.push(erroDados);atualizarEtapa('dados','error',erroDados);
        }

        if(podeImportarItens&&itensCarregados){
          atualizarEtapa('itens','done',itens.length+' itens recebidos');
        }else if(podeImportarItens){
          houveErro=true;const erroItens=resumirErro('itens',resultadoItens&&resultadoItens.erro);
          erros.push(erroItens);atualizarEtapa('itens','error',erroItens);
        }

        if(!dadosCarregados||!itensCarregados){
          atualizarEtapa('formulario','error','Importação cancelada: nenhum dado foi aplicado');
          if(resumoCarregamento)resumoCarregamento.textContent='Consulta incompleta';
          mostrar('Consulta cancelada para evitar um cadastro incompleto. Nenhum dado foi aplicado. '+erros.join(' '),'danger');
        }else{
          atualizarEtapa('formulario','active','Aplicando as informações recebidas');
          ['orgao','numeroEdital','uasg','modalidade','objeto','dataDisputa','valorEstimadoTotal','linkEdital'].forEach(nome=>{
            const entrada=campo(nome),valor=dados[nome];
            if(entrada&&valor!==null&&valor!==undefined&&valor!=='')entrada.value=valor;
          });
          if(podeImportarItens){limparItens();itensPncpJson.value=JSON.stringify(itens);linkItensPncp.value=link;exibirItens(itens)}
          if(dados.dataDisputa)avisoDataPncp.classList.remove('d-none');
          atualizarEtapa('formulario','done','Formulário preenchido por completo');
          if(resumoCarregamento)resumoCarregamento.textContent='Consulta concluída';
          mostrar(podeImportarItens?'Dados e '+itens.length+' itens carregados do PNCP. Confira as informações antes de cadastrar.':'Dados carregados do PNCP. Confira as informações antes de salvar.','success');
        }
      }catch(erro){
        houveErro=true;atualizarEtapa('formulario','error','Falha inesperada ao processar a resposta');
        if(resumoCarregamento)resumoCarregamento.textContent='Falha na consulta';
        mostrar(erro.message||'Não foi possível consultar o PNCP. Você ainda pode preencher a licitação manualmente.','danger');
      }finally{
        window.clearTimeout(temporizadorDemora);
        await new Promise(resolve=>window.setTimeout(resolve,houveErro?1800:700));
        botaoPncp.disabled=false;rotulo.textContent=rotuloOriginal;if(carregandoPncp)carregandoPncp.classList.add('d-none');document.body.removeAttribute('aria-busy');
      }
    });
  }
  const kanban=document.getElementById('licitacao-kanban');
  if(kanban){
    let dragged=null;
    kanban.querySelectorAll('.licitacao-card[draggable="true"]').forEach(card=>{
      card.addEventListener('dragstart',()=>{dragged=card;card.classList.add('dragging')});
      card.addEventListener('dragend',()=>card.classList.remove('dragging'));
    });
    kanban.querySelectorAll('.kanban-column:not(.andamento)').forEach(coluna=>{
      coluna.addEventListener('dragover',evento=>{evento.preventDefault();coluna.classList.add('drag-over')});
      coluna.addEventListener('dragleave',()=>coluna.classList.remove('drag-over'));
      coluna.addEventListener('drop',async evento=>{
        evento.preventDefault();coluna.classList.remove('drag-over');if(!dragged)return;
        const fluxo=['CADASTRO','COTACAO','APROVACAO','DEFINICAO','PARTICIPACAO'];
        const origem=dragged.dataset.etapa,destino=coluna.dataset.etapa;
        if(Math.abs(fluxo.indexOf(origem)-fluxo.indexOf(destino))!==1){alertaBonito('Movimentação não permitida','Só é permitido mover para a etapa imediatamente anterior ou seguinte.');return}
        const body=new URLSearchParams({destino});
        const headers={'Content-Type':'application/x-www-form-urlencoded'};
        if(window.licitacoesCsrf)headers[window.licitacoesCsrf.header]=window.licitacoesCsrf.token;
        try{
          const respostaHttp=await fetch('/licitacoes/'+dragged.dataset.id+'/etapa-kanban',{method:'POST',headers,body});
          let resposta={};try{resposta=await respostaHttp.json()}catch(ignorado){}
          if(respostaHttp.ok){location.reload();return}
          const mensagem=resposta.erro||'Não foi possível alterar a etapa.';
          const exigeSelecao=mensagem.includes('selecione os itens');
          alertaBonito(exigeSelecao?'Seleção de itens necessária':'Não foi possível mover a licitação',mensagem,exigeSelecao?'info':'error');
        }catch(erro){alertaBonito('Falha de comunicação','Não foi possível alterar a etapa. Tente novamente.','error')}
      });
    });
  }
  document.querySelectorAll('.editar-item-btn').forEach(botao=>botao.addEventListener('click',()=>{
    const alvo=document.querySelector(botao.getAttribute('data-target'));
    if(!alvo)return;
    window.setTimeout(()=>{if(alvo.classList.contains('show')){alvo.scrollIntoView({behavior:'smooth',block:'nearest'});const primeiroCampo=alvo.querySelector('input:not([type="hidden"])');if(primeiroCampo)primeiroCampo.focus()}},350);
  }));
  const selecionarTodosCotacao=document.getElementById('selecionar-todos-cotacao'),caixasCotacao=[...document.querySelectorAll('.item-cotacao-selecao:not(:disabled)')],contadorCotacao=document.getElementById('contador-itens-cotacao'),formAvancarCotacao=document.getElementById('form-avancar-cotacao'),botaoAvancarCotacao=document.getElementById('avancar-cotacao');
  if(formAvancarCotacao){
    const atualizarSelecaoCotacao=()=>{
      const quantidade=caixasCotacao.filter(caixa=>caixa.checked).length;
      if(contadorCotacao){contadorCotacao.textContent=quantidade+' de '+caixasCotacao.length+' itens selecionados';contadorCotacao.classList.toggle('text-danger',quantidade===0)}
      if(selecionarTodosCotacao){selecionarTodosCotacao.checked=caixasCotacao.length>0&&quantidade===caixasCotacao.length;selecionarTodosCotacao.indeterminate=quantidade>0&&quantidade<caixasCotacao.length}
      if(botaoAvancarCotacao)botaoAvancarCotacao.disabled=quantidade===0;
      caixasCotacao.forEach(caixa=>{const linha=caixa.closest('tr');if(linha)linha.classList.toggle('text-muted',!caixa.checked)});
    };
    caixasCotacao.forEach(caixa=>caixa.addEventListener('change',atualizarSelecaoCotacao));
    if(selecionarTodosCotacao)selecionarTodosCotacao.addEventListener('change',()=>{caixasCotacao.forEach(caixa=>caixa.checked=selecionarTodosCotacao.checked);atualizarSelecaoCotacao()});
    formAvancarCotacao.addEventListener('submit',evento=>{if(!caixasCotacao.some(caixa=>caixa.checked)){evento.preventDefault();atualizarSelecaoCotacao()}});
    atualizarSelecaoCotacao();
  }
  const area=document.getElementById('dados-importacao'),delim=document.getElementById('delimitador-importacao'),preview=document.getElementById('preview-importacao');
  if(area&&preview){
    const separar=(linha,d)=>{
      if(d==='ESPACOS')return linha.trim().split(/\s{2,}/);
      const separador=d==='TAB'?'\t':d==='PONTO_VIRGULA'?';':',';
      const colunas=[];let atual='',aspas=false;
      for(let i=0;i<linha.length;i++){const c=linha[i];if(c==='"'){if(aspas&&linha[i+1]==='"'){atual+='"';i++}else aspas=!aspas}else if(c===separador&&!aspas){colunas.push(atual);atual=''}else atual+=c}
      colunas.push(atual);return colunas;
    };
    const atualizar=()=>{const linhas=area.value.trim().split(/\r?\n/).filter(Boolean);if(!linhas.length){preview.innerHTML='';return}let d=delim.value;if(d==='AUTOMATICO')d=linhas[0].includes('\t')?'TAB':linhas[0].includes(';')?'PONTO_VIRGULA':linhas[0].includes(',')?'VIRGULA':'ESPACOS';const dados=linhas.slice(0,6).map(x=>separar(x,d));const n=Math.max(...dados.map(x=>x.length));document.querySelectorAll('.map-col').forEach(sel=>{const atual=sel.value,def=Number(sel.dataset.default);sel.innerHTML='<option value="-1">Ignorar coluna</option>'+Array.from({length:n},(_,i)=>'<option value="'+i+'">Coluna '+(i+1)+'</option>').join('');sel.value=atual||String(def<n?def:-1)});preview.innerHTML=dados.map((r,i)=>'<tr'+(i===0?' class="bg-light"':'')+'>'+r.map(c=>'<td>'+c.replace(/[&<>]/g,x=>({'&':'&amp;','<':'&lt;','>':'&gt;'}[x]))+'</td>').join('')+'</tr>').join('')};area.addEventListener('input',atualizar);delim.addEventListener('change',atualizar);atualizar();
  }
})();

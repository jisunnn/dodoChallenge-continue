package com.green.sdsd;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import service.NoticeService;
import vo.NoticeVO;
import vo.PageVO;

@Controller
public class NoticeController {
	
	@Autowired
	NoticeService service;
     
	
	// ** Notice PageList 1.
		@RequestMapping(value = "/npagelist")
		public ModelAndView bpagelist(ModelAndView mv, PageVO<NoticeVO> pvo) {
			
			int currPage = 1;
			if (pvo.getCurrPage() > 1) currPage = pvo.getCurrPage();
			else pvo.setCurrPage(currPage) ;
			
			int sno = (currPage-1)*pvo.getRowsPerPage() + 1 ;
			int eno = sno + pvo.getRowsPerPage() - 1 ;
			pvo.setSno(sno);
			pvo.setEno(eno);
			
			// 2) Service 처리
			// => List 읽어오기, 전체Row수(totalRowCount) 
			// => 전체 PageNo 계산하기
			pvo = service.pageList(pvo) ;
			int totalPageNo = pvo.getTotalRowCount()/pvo.getRowsPerPage();
			// 20/3 -> 6 나머지 2 : 6 page 와 2개 -> 7page
			if ( pvo.getTotalRowCount()%pvo.getRowsPerPage() !=0 )
				totalPageNo +=1;
			
			int sPageNo = ((currPage-1)/pvo.getPageNoCount())*pvo.getPageNoCount() + 1 ;
			int ePageNo = sPageNo + pvo.getPageNoCount() - 1 ;
			// 계산으로 얻어진 ePageNo가 실제 LastPage 인 totalPageNo 보다 크면 수정 필요.
			if ( ePageNo > totalPageNo ) ePageNo = totalPageNo ;
			mv.addObject("sPageNo", sPageNo);
			mv.addObject("ePageNo", ePageNo);
			mv.addObject("pageNoCount", pvo.getPageNoCount());
			
			// ** view01
			mv.addObject("currPage",currPage);
			mv.addObject("totalPageNo",totalPageNo);
			mv.addObject("nbanana", pvo.getList());
			
			mv.setViewName("board/pageNList");
			return mv;
		}//bpagelist
	
	
	
	@RequestMapping(value = "/nlist")
	public ModelAndView nlist(ModelAndView mv) {
		
		List<NoticeVO> list = new ArrayList<NoticeVO>();
    	list = service.selectList();
    	
    	// => Mapper 는 null 을 return 하지 않으므로 길이로 확인 
    	if ( list!=null && list.size()>0 ) mv.addObject("nbanana", list);
    	else mv.addObject("message", "~~ 출력 자료가 없습니다 ~~");
		
    	mv.setViewName("board/noticeList");
		return mv;
	} //nlist
	
	@RequestMapping(value = "/ndetail")
	public ModelAndView ndetail(HttpServletRequest request, ModelAndView mv, NoticeVO vo) {
		
		String uri = "board/noticeDetail";
		
		vo = service.selectOne(vo);
    	if ( vo!=null ) {
    		// 조회수 증가 추가
    		String loginID = (String)request.getSession().getAttribute("loginID") ;
    		if ( !vo.getId().equals(loginID) &&    
    			 !"U".equals(request.getParameter("jcode")) ) {
    			// 조회수 증가
    			if ( service.countUp(vo) > 0 )
    					vo.setCnt(vo.getCnt()+1) ;
    		} //if
    		
    		mv.addObject("napple", vo);
    		// 글 수정인지 확인
    		if ( "U".equals(request.getParameter("jcode")) ) 
    			uri = "board/nupdateForm";
    	}else {
    		mv.addObject("message", "~~ 글번호에 해당하는 자료가 없습니다 ~~");
    	}
		mv.setViewName(uri);
		return mv;
	} //ndetail
	
	
	@RequestMapping(value = "/ninsertf")
	public ModelAndView ninsertf(ModelAndView mv) {
		mv.setViewName("board/nInput");
		return mv;
	} //ninsertf
	
	@RequestMapping(value = "/ninsert")
	public ModelAndView ninsert(ModelAndView mv, NoticeVO vo, RedirectAttributes rttr) {
		
		String uri = "redirect:npagelist";
		if ( service.insert(vo) > 0 ) { 
    		// 글등록 성공 -> nlist , redirect
    		rttr.addFlashAttribute("message", "새 글이 등록되었습니다.");
    	}else {
    		mv.addObject("message", "새 글 등록에 실패하였습니다.");
    		uri = "board/nInput";
    	}
		mv.setViewName(uri);
		return mv;
	} //ninsert
	
	
	
	  @RequestMapping(value = "/nupdatef") 
	  public ModelAndView nupdatef(ModelAndView mv, NoticeVO vo) { 
		  vo = service.selectOne(vo);
		  mv.addObject("napple", vo);
		  mv.setViewName("board/nupdateForm"); 
		  return mv;
	  }
	
	@RequestMapping(value = "/nupdate")
	public ModelAndView nupdate(ModelAndView mv, NoticeVO vo, RedirectAttributes rttr) {
		String uri = "redirect:npagelist";
		  if ( service.update(vo) > 0 ) { 
			  rttr.addFlashAttribute("message", "~~ 글수정 성공 !!! ~~"); 
		 }else {
			 rttr.addFlashAttribute("message", "~~ 글수정 실패 !!! 다시 하세요 ~~");
			 uri ="redirect:ndetail?jcode=U&seq="+vo.getSeq(); 
	     }
		mv.setViewName(uri);
		return mv;
	} //nupdate
	
	
	
	@RequestMapping(value = "/ndelete")
	public ModelAndView ndelete(ModelAndView mv, NoticeVO vo, RedirectAttributes rttr) {
		String uri = "redirect:npagelist";
		if ( service.delete(vo) > 0 ) { 
    		// 글삭제 성공 -> nlist  : redirect
			rttr.addFlashAttribute("message", "글이 삭제되었습니다.");
    	}else {
    		rttr.addFlashAttribute("message", "글 삭제에 실패했습니다.");
    		uri = "redirect:ndetail?seq="+vo.getSeq();
    	}
		mv.setViewName(uri);
		return mv;
	} //ndelete	
	
}

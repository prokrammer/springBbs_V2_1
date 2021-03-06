package com.pknu.bbs.bbs.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.pknu.bbs.bbs.dto.BBSDto;
import com.pknu.bbs.bbs.join.BBSJoin;
import com.pknu.bbs.bbs.login.BBSLogin;
import com.pknu.bbs.bbs.reply.BBSReply;
import com.pknu.bbs.bbs.service.BBSService;
import com.pknu.bbs.bbs.write.BBSWrite;

@Controller
public class BBSController {
	
	@Autowired
	private BBSService bbsService;
	
	@Autowired
	private BBSLogin bbsLogin;
	
	@Autowired
	private BBSWrite bbswrite;
	
	
	@Autowired
	private BBSJoin bbsjoin;
	
	@Autowired
	private BBSReply bbsreply;
	
//	@Autowired
//	private FileSystemResource fileSystemResource;
//	@Autowired
//	private FileSystemResource fileSystemResource;
	/*
	 * @ModelAttribute는 파라미터 이름을 반드시 적어주자
	 * 그러면 객체가 아니고 프리미티브 타입이 와도 됨
	 * public String list(@ModelAttribute("pageNum") String pageNum, Model model){
	 * public String list(@ModelAttribute("pageNum") int pageNum, Model model){
	 * 아래코드는 에러가 남... 파라미터 이름을 생략하면 int를 객체로 생성할려고 해서 에러가 남
	 * public String list(@ModelAttribute int pageNum, Model model){
	 * 아래코드는 객체를 생성할려고 하는데  String 이니까 에러는 안나지만... 파라미터가 안넘어옴
	 * public String list(@ModelAttribute String pageNum, Model model){
	 */
	@RequestMapping(value="/list.bbs")
	@Transactional
		public String list(@ModelAttribute("pageNum") int pageNum, Model model) {
//		model.addAttribute("pageNum", pageNum);
		bbsService.list(pageNum, model);
		return "list";
		
	}
	
	@RequestMapping(value="/joinForm.bbs")
	public String joinForm(String pageNum, Model model) {
		model.addAttribute("pageNum", pageNum);
		
		return "joinform";
	}
	
	@RequestMapping(value="/join.bbs")
	public String join(Model model,String id, String pass) {
		
		bbsjoin.join(model,id,pass);
		return "redirect:list.bbs?pageNum=1";
	}
	
	@RequestMapping(value="/write.bbs", method=RequestMethod.GET)
	@Transactional
	public String writeForm(HttpSession session, HttpServletRequest req) {
		/*if((String)session.getAttribute("id")==null){
		req.setAttribute("pageNum", "1");
			return "login";
		}*/
		return "writeForm";
	}
//	value값은 method를 요청하지 않을 경우 굳이 안써도 된다
//	@Transactional(readOnly=false)
/*	@RequestMapping(value="/write.bbs", method=RequestMethod.POST)
	public String write(BBSDto article, HttpSession session, MultipartHttpServletRequest mRequest) {
		article.setId((String)session.getAttribute("id"));
		bbswrite.write(article,mRequest);
		return "redirect://list.bbs?pageNum=1";
	}
*/	@RequestMapping(value="/write.bbs", method=RequestMethod.POST)
	@Transactional
	public String write(BBSDto article, HttpSession session) {
		article.setId((String)session.getAttribute("id"));
		if(article.getContent().isEmpty() || article.getTitle().isEmpty()) {
			return "writeForm";
		}
		bbswrite.write(article);
		
		return "redirect://list.bbs?pageNum=1";
	}
	@RequestMapping(value="/download.bbs")
	@ResponseBody
	public FileSystemResource download(@RequestParam String storedFname, 
									HttpServletResponse resp) {
		return bbsService.download(resp, storedFname);
	}
	
	
	@RequestMapping(value="/content.bbs")
	@Transactional
	public String content(@RequestParam("pageNum") String pageNum, 
			@RequestParam String articleNum, 
			Model model, 
			@RequestParam("fileStatus") int fileStatus, 
			HttpServletRequest req,
			HttpSession session) {
		System.err.println(fileStatus);
		bbsService.content(fileStatus, articleNum, model);
		model.addAttribute("pageNum",pageNum);
		return "content";
	}
	@RequestMapping(value="/delete.bbs")
	public String delete(String articleNum,String pageNum, int fileStatus) {
			try {
				System.out.println("delete : fileStatus = " + fileStatus);
				bbsService.delete(articleNum, fileStatus);
			} catch (ServletException | IOException e) {
				e.printStackTrace();
			}
		
		return "redirect:list.bbs?pageNum="+pageNum;
	}
	
	@RequestMapping(value="/update.bbs", method=RequestMethod.GET)
	public String updateForm(@ModelAttribute("articleNum") String articleNum, 
			@ModelAttribute("pageNum") String pageNum, 
			@ModelAttribute("fileStatus") int fileStatus,
			Model model) {
//		model.addAttribute("articleNum", articleNum);
//		model.addAttribute("pageNum", pageNum);
		try {
			bbsService.updateForm(articleNum, fileStatus, model);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		return "updateForm";
	}
	
	@RequestMapping(value="/update.bbs", method=RequestMethod.POST)
//	아래와 같이 사용시에는 오류가 남
//	ArrayList<String> storedFnameList, ..set,get 메소드가 없음
//	UpdateDto를 만들어서 set,get 메소드를 사용해야지
//	jsp에서 복수개의 name 속성을 가지는 파라미터를 받을 수 있음 
	public String update(
			BBSDto article, 
//			UpdateDto updateDto,
//			@RequestParam을 넣으면 값이 반드시 넘어와야 한다.
			String[] deleteFileNames,
			String pageNum, 
			Model model, 
			int fileCount
			) {
		if(article.getFileNames()!=null) {
			for(String storedFname : article.getFileNames()) {
				System.err.println("article.getFileNames() : " + storedFname);
			}
		}
		bbsService.update(article, deleteFileNames, model, fileCount);
		/*for(String storedFname : storedFnameList) {
			System.out.println("StringList[] : " + storedFname);
			
		}*/
		/*for(String storedFname : updateDto.getStoredFnameList()) {
			System.out.println("updateDto : " + storedFname);
		}*/
		
		/*try {
//			bbsService.update(model, articleNum, title, content);
		} catch (ServletException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return "redirect:list.bbs?pageNum="+pageNum;
	}
	
	@RequestMapping(value="/replyForm.bbs")
	public String replyForm(BBSDto bd, Model model,@ModelAttribute("pageNum") String pageNum) {
		System.out.println(bd);
		model.addAttribute("replyDto", bd);
		return "replyForm"; 
	}
	
	@RequestMapping(value="/reply.bbs", method=RequestMethod.POST)
	public String reply(String pageNum, Model model, BBSDto article,HttpSession session ,@RequestPart("fileData") List<MultipartFile> mfile) {
		String id = (String)session.getAttribute("id");
		try {
			bbsreply.reply(model, article, id, mfile);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		return "redirect:list.bbs?pageNum="+pageNum;
	}
	@RequestMapping(value="/loginForm.bbs")
	public String loginForm(HttpServletRequest req, Model model) {
		return "login";
	}
	@RequestMapping(value="/login.bbs"/*, method=RequestMethod.POST*/)
	public String login(HttpServletRequest req, HttpServletResponse resp) {
		String view=null;
		try {
			view = bbsLogin.loginCheck(req);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return view;
	}

	@RequestMapping(value="/logout.bbs")
	public String logout(HttpSession session, String pageNum) {
		session.invalidate();
		return "redirect:list.bbs?pageNum="+pageNum;
	}

}

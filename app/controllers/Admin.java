package controllers;

import java.util.List;

import models.Post;
import models.Tag;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Admin extends Controller {

	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			User user = User.find("byEmail", Security.connected()).first();
			renderArgs.put("user", user.fullname);
		}
	}
	
	public static void index() {
		String user = Security.connected();
		List<Post> posts =Post.find("author.email", user).fetch();
		render(posts);
	}
	
	public static void form(Long id) {
		if (id != null) {
			Post post = Post.findById(id);
			render(post);
			return;
		}
		render();
	}
	
	public static void save(Long id, String title, String content, String tags) {
		Post post;
		if (id == null) {
			// create post
			User author = User.find("byEmail", Security.connected()).first();
			post = new Post(author, title, content);
		} else {
			// edit post
			post = Post.findById(id);
			post.title = title;
			post.content = content;
			post.tags.clear();
		}
		
		// set tags
		for(String tag : tags.split("\\s+")) {
			if (tag.trim().length() > 0) {
				post.tags.add(Tag.findOrCreateByName(tag));
			}
		}
		
		validation.valid(post);
		if (validation.hasErrors()) {
			render("@form", post);
			return;
		}
		
		post.save();
		index();
	}
}

// System Design - Backend
// Chapter 18, Scaling & Performance -> 09 The N+1 query problem
// Java 21 / Spring Boot 3.3

package com.example.scaling.nplusone;

// With JPA you usually do not write the merge at all. The N+1 comes
// from lazy-loading each association one row at a time, so tell the
// query to fetch it in the SAME round-trip:
interface PostRepository extends JpaRepository<Post, Long> {
    
    @EntityGraph(attributePaths = "author")
    List<Post> findAll();
}

@Service
class PostFeed {
    
    private final PostRepository posts;
    private final AuthorRepository authors;
    
    PostFeed(PostRepository posts, AuthorRepository authors) {
        this.posts = posts;
        this.authors = authors;
    }
    
    List<PostWithAuthor> feed() {
        // Query 1: the posts.
        List<Post> found = posts.findAll();
        
        // Query 2: every author at once, not one lookup per post.
        Set<Long> authorIds = found.stream()
                                  .map(Post::authorId)
                                  .collect(Collectors.toSet());
        
        Map<Long, Author> byId = authors.findAllById(authorIds).stream()
                                     .collect(Collectors.toMap(Author::id, author -> author));
        
        return found.stream()
                   .map(post -> new PostWithAuthor(post, byId.get(post.authorId())))
                   .toList();
    }
}

/**
 * 
 */
package json;

/**
 * @author skrawczy
 *
 */
public class Profile
{
  public String firstName;
  public String lastName;
  public String id;
  public String headline;
  public String pictureUrl;
  public String summary;
  public String industry;
  public Profile(String firstName,
                 String lastName,
                 String id,
                 String headline,
                 String pictureUrl,
                 String summary,
                 String industry)
  {
    super();
    this.firstName = firstName;
    this.lastName = lastName;
    this.id = id;
    this.headline = headline;
    this.pictureUrl = pictureUrl;
    this.summary = summary;
    this.industry = industry;
  }
  
}

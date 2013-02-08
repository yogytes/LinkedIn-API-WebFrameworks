package controllers;

import java.util.Comparator;

import json.Connections;
import json.Profile;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;

import play.*;
import play.mvc.*;
import scala.actors.threadpool.Arrays;

import views.html.*;

public class Application extends Controller {
  
  
  public static Result auth() {
    //get the service
    OAuthService serv = getOAuthService();
    //make a request token
    Token requestToken = serv.getRequestToken();
    //store the relevant stuff in a cookie
    session("req_token", requestToken.getToken());
    session("req_secret", requestToken.getSecret());
    //get the url
    String authUrl = serv.getAuthorizationUrl(requestToken);
    //redirect
    return redirect(authUrl);
  }

  public static Result callback(String oauth_token,
                                String oauth_verifer)
  {
    //if there's nothing in the cookie reroute
    if(session("req_token") == null || session("req_secret") == null)
      return redirect(controllers.routes.Application.auth());

    //create the access token
    Verifier verifier = new Verifier(oauth_verifer);
    OAuthService serv = getOAuthService();
    Token accessToken = serv.getAccessToken(new Token(session("req_token"),session("req_secret")),
                                            verifier);
    //store it
    session("acc_token",accessToken.getToken());
    session("acc_secret",accessToken.getSecret());
    //redirect
    return redirect(controllers.routes.Application.index());
  }
  
  public static Result index()
  {
    //if not logged in redirect through auth
    if(session("acc_token") == null || session("acc_secret") == null) 
      return redirect(controllers.routes.Application.auth());
    //make api call
    Token at = new Token(session("acc_token"),session("acc_secret"));
    String profileData = getProfileData(at).getBody();
    Gson gson = new Gson();
    Profile profile = gson.fromJson(profileData, Profile.class);
    String connectionsData = getConnectionData(at).getBody();
    Connections connections = gson.fromJson(connectionsData, Connections.class);
    Arrays.sort(connections.values, new Comparator<Profile>()
    {
      @Override
      public int compare(Profile o1, Profile o2)
      {
        return o1.firstName.compareTo(o2.firstName);
      }
    });
    return ok(index.render(profile,connections));
  }

  private static Response getProfileData(Token accessToken)
  {
    String fields = "(id,first-name,last-name,headline,picture-url,summary,industry)";
    String requestUrl = "http://api.linkedin.com/v1/people/~:"+fields+"?format=json";
    OAuthRequest req = new OAuthRequest(Verb.GET, requestUrl);
    OAuthService serv = getOAuthService();
    serv.signRequest(accessToken, req);
    Response res = req.send();
    return res;
  }
  
  private static Response getConnectionData(Token accessToken)
  {
    String fields = "(id,first-name,last-name,headline,picture-url,summary,industry)";
    String requestUrl = "http://api.linkedin.com/v1/people/~/connections:"+fields+"?format=json";
    OAuthRequest req = new OAuthRequest(Verb.GET, requestUrl);
    OAuthService serv = getOAuthService();
    serv.signRequest(accessToken, req);
    Response res = req.send();
    return res;
  }
  
  private static OAuthService getOAuthService()
  {
    ServiceBuilder sb = new ServiceBuilder()
      .provider(LinkedInApi.class)
      .apiKey(Configuration.root().getString("linkedin.apiKey"))
      .apiSecret(Configuration.root().getString("linkedin.apiSecret"))
      .callback(Configuration.root().getString("linkedin.callback"));
    OAuthService serv = sb.build();
    return serv;
  }
}
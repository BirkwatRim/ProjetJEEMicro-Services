package org.sid.secservice.sec.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sid.secservice.sec.JWTUtil;
import org.sid.secservice.sec.entities.AppRole;
import org.sid.secservice.sec.entities.AppUser;
import org.sid.secservice.sec.filter.JwtAuthorizationFilter;
import org.sid.secservice.sec.service.AccountService;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AccountRestController {
    private AccountService accountService;

    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(path = "/users")
    @PostAuthorize("hasAuthority('USER')")
    public List<AppUser> appUsers()
    {
         return accountService.listUsers();
    }

    @PostMapping(path = "/users")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppUser saveUser(@RequestBody AppUser appUser){
      return accountService.addNewUser(appUser);
    }

    @PostMapping(path = "/roles")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppRole saveRole(@RequestBody AppRole appRole){
        return accountService.addNewRole(appRole);
    }
    @PostMapping(path = "/addRoleToUser")
    public void addRoleToUser(@RequestBody RoleUserForm roleUserForm){
        accountService.addRoleToUser(roleUserForm.getUsername(),roleUserForm.getRolename());
    }
    @GetMapping(path = "/refreshToken")
   public void refreshToken(HttpServletRequest request , HttpServletResponse response)throws Exception
    {
        String auhToken=request.getHeader(JWTUtil.AUTH_HEDER);
        if(auhToken!=null&&auhToken.startsWith(JWTUtil.PREFIX))
        {
            try {
                String jwt=auhToken.substring(JWTUtil.PREFIX.length());
                Algorithm algorithm=Algorithm.HMAC256(JWTUtil.SECRET );
                JWTVerifier jwtVerifier= JWT.require(algorithm).build();
                DecodedJWT decodedJWT=jwtVerifier.verify(jwt);
                String username=decodedJWT.getSubject();
               AppUser appUser=accountService.loadUserByUsername(username);
                Algorithm algo1=Algorithm.HMAC256(JWTUtil.SECRET);
                String jwtAccesToken= JWT.create()
                        .withSubject(appUser.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis()+JWTUtil.EXPIRE_ACCESS_TOKEN ))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles",appUser.getAppRoles().stream().map(r ->r.getRoleName()).collect(Collectors.toList() ))
                        .sign(algo1);


                Map<String,String> idToken=new HashMap<>();
                idToken.put("access-token",jwtAccesToken);
                idToken.put("refresh-token",jwt);
                new ObjectMapper().writeValue(response.getOutputStream(),idToken);

                response.setContentType("application/json");
            }
            catch (Exception e)
            {
              throw e;

            }


        }
        else
        {
            throw new RuntimeException("Refrech token required!!!");
        }
    }

    }

import { keycloak } from '../config/keycloak';

export const getCurrentUser = () => {
  return {
    username: keycloak.tokenParsed.preferred_username,
    email: keycloak.tokenParsed.email,
  }
}

export const isAdminFunc = (keycloak) => {
  return keycloak && 
         keycloak.tokenParsed &&
         keycloak.tokenParsed.resource_access['moviebets-app'] &&
         keycloak.tokenParsed.resource_access['moviebets-app'].roles.includes('MOVIEBETS_MANAGER')
}

export const getUsernameFunc = (keycloak) => {
  return keycloak.tokenParsed.preferred_username
}

export const isUserFunc = (keycloak) => {
  return keycloak && 
         keycloak.tokenParsed &&
         keycloak.tokenParsed.resource_access['moviebets-app'] &&
         keycloak.tokenParsed.resource_access['moviebets-app'].roles.includes('MOVIEBETS_USER')
}

export const handleLogError = (error) => {
  if (error.response) {
    console.log(error.response.data);
  } else if (error.request) {
    console.log(error.request);
  } else {
    console.log(error.message);
  }
}

export const bearerAuth = (keycloak) => {
  return `Bearer ${keycloak.token}`
}
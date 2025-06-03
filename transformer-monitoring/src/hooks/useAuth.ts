import { useState, useEffect, createContext, useContext } from 'react';
import { User } from '@/types';
import { apiService } from '@/services/api';

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  isLoading: boolean;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const useAuthProvider = () => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check for stored user session
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string): Promise<boolean> => {
    setIsLoading(true);
    
    try {
      const users = await apiService.fetchUsers();
      const foundUser = users.find(u => u.username === username);
      
      if (foundUser) {
        // In a real app, you'd verify the password hash
        // For demo purposes, any password works for existing usernames
        const userWithLastLogin = {
          ...foundUser,
          last_login: new Date().toISOString()
        };
        
        setUser(userWithLastLogin);
        localStorage.setItem('currentUser', JSON.stringify(userWithLastLogin));
        setIsLoading(false);
        return true;
      }
      
      setIsLoading(false);
      return false;
    } catch (error) {
      console.error('Login error:', error);
      setIsLoading(false);
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('currentUser');
  };

  return {
    user,
    login,
    logout,
    isLoading
  };
};

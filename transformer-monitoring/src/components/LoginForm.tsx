import { useState } from 'react';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useAuth } from '@/hooks/useAuth';
import { Zap, Lock, User } from 'lucide-react';

export const LoginForm = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    const success = await login(username, password);
    
    if (!success) {
      setError('Username atau password tidak valid');
    }
    
    setIsLoading(false);
  };

  const demoUsers = [
    { username: 'admin', role: 'Administrator' },
    { username: 'operator1', role: 'Operator' },
    { username: 'viewer1', role: 'Viewer' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background Effects */}
      <div className="absolute inset-0 bg-[url('/images/circuit-pattern.jpg')] opacity-5"></div>
      <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/10 via-purple-500/10 to-blue-500/10"></div>
      
      {/* Animated Background Elements */}
      <motion.div
        className="absolute top-1/4 left-1/4 w-64 h-64 bg-cyan-500/20 rounded-full blur-3xl"
        animate={{
          scale: [1, 1.2, 1],
          opacity: [0.3, 0.5, 0.3],
        }}
        transition={{
          duration: 4,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />
      <motion.div
        className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl"
        animate={{
          scale: [1.2, 1, 1.2],
          opacity: [0.2, 0.4, 0.2],
        }}
        transition={{
          duration: 5,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-md relative z-10"
      >
        <Card className="backdrop-blur-xl bg-black/20 border-cyan-500/20 shadow-2xl shadow-cyan-500/10">
          <CardHeader className="text-center space-y-4">
            <motion.div
              initial={{ scale: 0.8 }}
              animate={{ scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="flex justify-center"
            >
              <div className="w-16 h-16 bg-gradient-to-br from-cyan-400 to-blue-500 rounded-xl flex items-center justify-center shadow-lg shadow-cyan-500/25">
                <Zap className="w-8 h-8 text-white" />
              </div>
            </motion.div>
            
            <div>
              <CardTitle className="text-2xl font-bold bg-gradient-to-r from-cyan-400 to-blue-400 bg-clip-text text-transparent">
                PLN Transformer Monitor
              </CardTitle>
              <CardDescription className="text-slate-400 mt-2">
                Sistem Monitoring Transformer Gaming Style
              </CardDescription>
            </div>
          </CardHeader>

          <CardContent className="space-y-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="username" className="text-slate-300 font-medium">
                  Username
                </Label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <Input
                    id="username"
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="pl-10 bg-black/20 border-slate-600/50 text-white placeholder-slate-400 focus:border-cyan-400 focus:ring-cyan-400/20"
                    placeholder="Masukkan username"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-slate-300 font-medium">
                  Password
                </Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <Input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-10 bg-black/20 border-slate-600/50 text-white placeholder-slate-400 focus:border-cyan-400 focus:ring-cyan-400/20"
                    placeholder="Masukkan password"
                    required
                  />
                </div>
              </div>

              {error && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                >
                  <Alert className="bg-red-500/10 border-red-500/20 text-red-400">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                </motion.div>
              )}

              <Button
                type="submit"
                disabled={isLoading}
                className="w-full bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-600 hover:to-blue-600 text-white font-semibold py-2 px-4 rounded-lg transition-all duration-200 transform hover:scale-105 shadow-lg shadow-cyan-500/25 disabled:opacity-50 disabled:transform-none"
              >
                {isLoading ? (
                  <motion.div
                    animate={{ rotate: 360 }}
                    transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                    className="w-5 h-5 border-2 border-white border-t-transparent rounded-full"
                  />
                ) : (
                  'Masuk Sistem'
                )}
              </Button>
            </form>

            {/* Demo Users Info */}
            <div className="pt-4 border-t border-slate-600/30">
              <p className="text-sm text-slate-400 text-center mb-3">Demo Users:</p>
              <div className="space-y-2">
                {demoUsers.map((user) => (
                  <motion.button
                    key={user.username}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => {
                      setUsername(user.username);
                      setPassword('demo');
                    }}
                    className="w-full text-left p-2 rounded-lg bg-slate-800/30 hover:bg-slate-700/30 border border-slate-600/20 hover:border-slate-500/30 transition-all"
                  >
                    <div className="text-sm text-cyan-300 font-medium">{user.username}</div>
                    <div className="text-xs text-slate-400">{user.role}</div>
                  </motion.button>
                ))}
              </div>
              <p className="text-xs text-slate-500 text-center mt-3">
                Password untuk semua user demo: "demo"
              </p>
            </div>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
};

import React, { useState, useEffect } from "react";

interface PhoneFrameProps {
  children: React.ReactNode;
}

export default function PhoneFrame({ children }: PhoneFrameProps) {
  const [time, setTime] = useState("");

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      let hours = now.getHours();
      const minutes = now.getMinutes().toString().padStart(2, "0");
      const ampm = hours >= 12 ? "PM" : "AM";
      hours = hours % 12;
      hours = hours ? hours : 12; // the hour '0' should be '12'
      setTime(`${hours}:${minutes} ${ampm}`);
    };
    updateTime();
    const interval = setInterval(updateTime, 60000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center p-0 md:p-6 select-none overflow-hidden font-sans">
      {/* Decorative background gradients for desktop */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden z-0 hidden md:block">
        <div className="absolute top-[10%] left-[20%] w-[35rem] h-[35rem] rounded-full bg-journal-primary/15 blur-[120px]"></div>
        <div className="absolute bottom-[10%] right-[20%] w-[35rem] h-[35rem] rounded-full bg-journal-teal/10 blur-[120px]"></div>
      </div>

      {/* Phone Chassis container */}
      <div className="relative w-full h-screen md:w-[410px] md:h-[840px] md:rounded-[48px] md:border-[10px] md:border-[#1E293B] md:shadow-2xl bg-journal-bg overflow-hidden flex flex-col z-10 transition-all duration-300">
        
        {/* Android Status Bar (Only visible/styled on simulated view) */}
        <div className="h-10 bg-journal-bg flex items-center justify-between px-6 text-slate-900 font-bold text-xs select-none z-40 shrink-0">
          <div className="flex items-center gap-1">
            <span>{time || "9:41 AM"}</span>
          </div>
          
          {/* Camera Notch on Desktop */}
          <div className="hidden md:block w-24 h-4 bg-[#1E293B] rounded-full absolute top-1 left-1/2 -translate-x-1/2 z-50"></div>
          
          <div className="flex items-center gap-1.5">
            <div className="w-2.5 h-2.5 rounded-full bg-slate-900"></div>
            <div className="w-2.5 h-2.5 rounded-full bg-slate-400"></div>
          </div>
        </div>

        {/* Dynamic App Content */}
        <div className="flex-1 flex flex-col overflow-hidden relative">
          {children}
        </div>

        {/* Android Bottom Navigation Pill */}
        <div className="h-6 bg-journal-bg flex items-center justify-center z-40 shrink-0">
          <div className="w-32 h-1.5 bg-slate-300 rounded-full"></div>
        </div>
      </div>
    </div>
  );
}
